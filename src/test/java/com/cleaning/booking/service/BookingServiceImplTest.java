package com.cleaning.booking.service;

import com.cleaning.booking.dto.BookingRequest;
import com.cleaning.booking.dto.BookingResponse;
import com.cleaning.booking.entity.Booking;
import com.cleaning.booking.entity.CleanerProfessional;
import com.cleaning.booking.entity.Vehicle;
import com.cleaning.booking.exception.*;
import com.cleaning.booking.repository.BookingRepository;
import com.cleaning.booking.repository.CleanerRepository;
import com.cleaning.booking.service.impl.BookingServiceImpl;
import com.cleaning.booking.util.WorkHoursValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceImplTest {

    private CleanerRepository cleanerRepository;
    private BookingRepository bookingRepository;
    private BookingServiceImpl bookingService;

    private final LocalDate validDate = LocalDate.of(2025, 1, 14); // Tuesday
    private final LocalTime validStart = LocalTime.of(10, 0);

    @BeforeEach
    void setup() {
        cleanerRepository = mock(CleanerRepository.class);
        bookingRepository = mock(BookingRepository.class);
        bookingService = new BookingServiceImpl(cleanerRepository, bookingRepository);
    }

    private CleanerProfessional cleaner(long id, long vehicleId) {
        CleanerProfessional c = new CleanerProfessional();
        c.setId(id);
        Vehicle v = new Vehicle();
        v.setId(vehicleId);
        c.setVehicle(v);
        return c;
    }

    /**
     * SUCCESS — One available cleaner, no conflicts
     */
    @Test
    void createBooking_success() {

        BookingRequest req = new BookingRequest(
                validDate,
                validStart,
                120,
                1
        );

        CleanerProfessional c1 = cleaner(1L, 10L);

        when(cleanerRepository.findAllWithVehicle())
                .thenReturn(List.of(c1));

        when(cleanerRepository.isCleanerAvailable(any(), any(), any(), any()))
                .thenReturn(true);

        when(cleanerRepository.lockCleanersForUpdate(List.of(1L)))
                .thenReturn(List.of(c1));

        when(bookingRepository.hasOverlap(any(), any(), any(), any()))
                .thenReturn(false);

        Booking b = new Booking();
        b.setId(99L);

        when(bookingRepository.save(any()))
                .thenReturn(b);

        BookingResponse response = bookingService.createBooking(req);

        assertNotNull(response);
        assertEquals(99L, response.bookingId());
        assertEquals(1, response.assignedCleaners().size());
        assertEquals(1L, response.assignedCleaners().get(0));

        verify(cleanerRepository, times(1)).findAllWithVehicle();
        verify(bookingRepository, times(1)).save(any());
    }

    /**
     * FAIL — Not enough available cleaners to satisfy cleanerCount
     */
    @Test
    void createBooking_failsNotEnoughCleaners() {

        BookingRequest req = new BookingRequest(
                validDate,
                validStart,
                120,
                2
        );

        CleanerProfessional c1 = cleaner(1L, 10L);

        when(cleanerRepository.findAllWithVehicle())
                .thenReturn(List.of(c1)); // only 1 cleaner available

        when(cleanerRepository.isCleanerAvailable(any(), any(), any(), any()))
                .thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(req));
    }

    /**
     * FAIL — Cleaners exist but not enough from same vehicle
     */
    @Test
    void createBooking_failsCleanersNotFromSameVehicle() {

        BookingRequest req = new BookingRequest(
                validDate,
                validStart,
                120,
                2
        );

        CleanerProfessional c1 = cleaner(1L, 10L);
        CleanerProfessional c2 = cleaner(2L, 20L); // different vehicle

        when(cleanerRepository.findAllWithVehicle())
                .thenReturn(List.of(c1, c2));

        when(cleanerRepository.isCleanerAvailable(any(), any(), any(), any()))
                .thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(req));
    }

    /**
     * FAIL — Overlap detected WITH 30-minute break rule applied
     */
    @Test
    void createBooking_failsOverlapBecauseBreakRule() {

        BookingRequest req = new BookingRequest(
                validDate,
                validStart,
                120,
                1
        );

        CleanerProfessional c1 = cleaner(1L, 10L);

        when(cleanerRepository.findAllWithVehicle())
                .thenReturn(List.of(c1));

        when(cleanerRepository.isCleanerAvailable(any(), any(), any(), any()))
                .thenReturn(true);

        when(cleanerRepository.lockCleanersForUpdate(List.of(1L)))
                .thenReturn(List.of(c1));

        // Simulate overlap AFTER expanded-window rule
        when(bookingRepository.hasOverlap(any(), any(), any(), any()))
                .thenReturn(true);

        assertThrows(OverlapException.class,
                () -> bookingService.createBooking(req));
    }

    /**
     * FAIL — Invalid: Friday is not a working day
     */
    @Test
    void createBooking_failsOnFriday() {

        BookingRequest req = new BookingRequest(
                LocalDate.of(2025, 1, 17), // FRIDAY
                validStart,
                120,
                1
        );

        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(req));
    }

    /**
     * FAIL — Invalid start time (before 08:00)
     */
    @Test
    void createBooking_failsInvalidStartTime() {
        BookingRequest req = new BookingRequest(
                validDate,
                LocalTime.of(7, 0),
                120,
                1
        );

        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(req));
    }

    /**
     * FAIL — Invalid end time (after 22:00)
     */
    @Test
    void createBooking_failsInvalidEndTime() {
        BookingRequest req = new BookingRequest(
                validDate,
                LocalTime.of(21, 30), // ends at 23:30
                120,
                1
        );

        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(req));
    }

    /**
     * FAIL — Duration not 120 or 240
     */
    @Test
    void createBooking_failsInvalidDuration() {
        BookingRequest req = new BookingRequest(
                validDate,
                validStart,
                90, // invalid
                1
        );

        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(req));
    }

    /**
     * SUCCESS — update booking
     */
    @Test
    void updateBooking_success() {

        Booking existing = new Booking();
        existing.setId(99L);

        BookingRequest req = new BookingRequest(validDate, validStart, 120, 1);

        when(bookingRepository.lockBookingForUpdate(99L))
                .thenReturn(existing);

        when(cleanerRepository.findAllWithVehicle())
                .thenReturn(List.of(cleaner(1L, 10L)));

        when(cleanerRepository.isCleanerAvailable(any(), any(), any(), any()))
                .thenReturn(true);

        when(cleanerRepository.lockCleanersForUpdate(List.of(1L)))
                .thenReturn(List.of(cleaner(1L, 10L)));

        when(bookingRepository.hasOverlap(any(), any(), any(), any()))
                .thenReturn(false);

        Booking saved = new Booking();
        saved.setId(77L);

        when(bookingRepository.save(any()))
                .thenReturn(saved);

        BookingResponse response = bookingService.updateBooking(99L, req);

        assertNotNull(response);
        assertEquals(77L, response.bookingId());
    }

    /**
     * FAIL — update booking not found
     */
    @Test
    void updateBooking_notFound() {

        when(bookingRepository.lockBookingForUpdate(99L))
                .thenReturn(null);

        BookingRequest req = new BookingRequest(validDate, validStart, 120, 1);

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.updateBooking(99L, req));
    }
}
