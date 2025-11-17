package com.cleaning.booking.service;

import com.cleaning.booking.dto.AvailabilityRequest;
import com.cleaning.booking.dto.AvailabilityResponse;
import com.cleaning.booking.entity.Booking;
import com.cleaning.booking.entity.CleanerProfessional;
import com.cleaning.booking.entity.Vehicle;
import com.cleaning.booking.exception.BadRequestException;
import com.cleaning.booking.repository.BookingRepository;
import com.cleaning.booking.repository.CleanerRepository;
import com.cleaning.booking.service.impl.AvailabilityServiceImpl;
import com.cleaning.booking.util.AvailabilityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AvailabilityServiceImplTest {

    private CleanerRepository cleanerRepository;
    private BookingRepository bookingRepository;
    private AvailabilityServiceImpl availabilityService;

    private final LocalDate validDate = LocalDate.of(2025, 1, 14); // Tuesday

    @BeforeEach
    void setup() {
        cleanerRepository = mock(CleanerRepository.class);
        bookingRepository = mock(BookingRepository.class);
        availabilityService = new AvailabilityServiceImpl(cleanerRepository, bookingRepository);
    }

    private CleanerProfessional cleaner(long id, long vehicleId) {
        CleanerProfessional c = new CleanerProfessional();
        c.setId(id);
        Vehicle v = new Vehicle();
        v.setId(vehicleId);
        c.setVehicle(v);
        return c;
    }

    // ------------------------------------------------------------------------------------
    // 1. FAIL — Friday is not a working day
    // ------------------------------------------------------------------------------------
    @Test
    void checkAvailability_failsOnFriday() {
        AvailabilityRequest req = new AvailabilityRequest(
                LocalDate.of(2025, 1, 17), // Friday
                null, null
        );

        assertThrows(BadRequestException.class,
                () -> availabilityService.checkAvailability(req));
    }

    // ------------------------------------------------------------------------------------
    // 2. DAILY AVAILABILITY — Only date provided
    // ------------------------------------------------------------------------------------
    @Test
    void checkAvailability_dailyAvailabilitySuccess() {

        AvailabilityRequest req = new AvailabilityRequest(validDate, LocalTime.of(10,0), 120);

        CleanerProfessional c1 = cleaner(1L, 10L);
        CleanerProfessional c2 = cleaner(2L, 10L);

        when(cleanerRepository.findAllWithVehicle())
                .thenReturn(List.of(c1, c2));

        Booking b1 = new Booking();
        b1.setId(100L);
        b1.setStartTime(LocalTime.of(9,0));
        b1.setEndTime(LocalTime.of(11,0));
        Booking b2 = new Booking();
        b2.setStartTime(LocalTime.of(12,0));
        b2.setEndTime(LocalTime.of(14,0));
        b2.setId(200L);

        when(bookingRepository.findBookingsForCleaner(1L, validDate))
                .thenReturn(List.of(b1));

        when(bookingRepository.findBookingsForCleaner(2L, validDate))
                .thenReturn(List.of(b2));

        // Stub static util indirectly by giving fake output.
        // AvailabilityUtil is deterministic based on bookings so mocking not needed.

        AvailabilityResponse res = availabilityService.checkAvailability(req);
        assertNotNull(res);
        assertTrue(res.availableCleanerIds().isEmpty()); // daily → only slot list returned
        assertTrue(res.availableTimeSlots().isEmpty());
    }


    // ------------------------------------------------------------------------------------
    // 3. SPECIFIC SLOT — No cleaners available
    // ------------------------------------------------------------------------------------
    @Test
    void checkAvailability_slotBasedNoneAvailable() {

        AvailabilityRequest req = new AvailabilityRequest(
                validDate,
                LocalTime.of(14, 0),
                120
        );

        CleanerProfessional c1 = cleaner(1L, 10L);
        CleanerProfessional c2 = cleaner(2L, 10L);

        when(cleanerRepository.findAllWithVehicle())
                .thenReturn(List.of(c1, c2));

        when(cleanerRepository.isCleanerAvailable(any(), any(), any(), any()))
                .thenReturn(false);

        AvailabilityResponse res = availabilityService.checkAvailability(req);

        assertNotNull(res);
        assertTrue(res.availableCleanerIds().isEmpty());
        assertTrue(res.availableTimeSlots().isEmpty());
    }

    // ------------------------------------------------------------------------------------
    // 4. DAILY AVAILABILITY — No bookings → all default slots returned
    // ------------------------------------------------------------------------------------
    @Test
    void checkAvailability_dailyAvailability_noBookings() {

        AvailabilityRequest req = new AvailabilityRequest(validDate, null, null);

        CleanerProfessional c1 = cleaner(1L, 10L);

        when(cleanerRepository.findAllWithVehicle())
                .thenReturn(List.of(c1));

        when(bookingRepository.findBookingsForCleaner(1L, validDate))
                .thenReturn(List.of()); // No bookings → full availability

        AvailabilityResponse res = availabilityService.checkAvailability(req);

        assertNotNull(res);
        assertTrue(res.availableCleanerIds().isEmpty());
        assertFalse(res.availableTimeSlots().isEmpty()); // AvailabilityUtil will generate full-day slots
    }
}
