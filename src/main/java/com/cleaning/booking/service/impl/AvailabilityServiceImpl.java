package com.cleaning.booking.service.impl;

import com.cleaning.booking.dto.AvailabilityRequest;
import com.cleaning.booking.dto.AvailabilityResponse;
import com.cleaning.booking.entity.Booking;
import com.cleaning.booking.entity.CleanerProfessional;
import com.cleaning.booking.exception.BadRequestException;
import com.cleaning.booking.repository.BookingRepository;
import com.cleaning.booking.repository.CleanerRepository;
import com.cleaning.booking.service.AvailabilityService;
import com.cleaning.booking.util.AvailabilityUtil;
import com.cleaning.booking.util.TimeWindow;
import com.cleaning.booking.util.WorkHoursValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final CleanerRepository cleanerRepository;
    private final BookingRepository bookingRepository;

    @Override
    public AvailabilityResponse checkAvailability(AvailabilityRequest req) {

        if (!WorkHoursValidator.isWorkingDay(req.date())) {
            throw new BadRequestException("Friday is not a working day.");
        }

        // CASE 1: Date only → return available slots
        if (req.startTime() == null && req.durationMinutes() == null) {
            return generateDailyAvailability(req.date());
        }

        // CASE 2: Specific slot → return available cleaner IDs
        return getCleanersForRequestedSlot(req);
    }

    /**
     * Returns all available time slots for the entire day (08:00–22:00)
     */
    private AvailabilityResponse generateDailyAvailability(LocalDate date) {

        List<CleanerProfessional> cleaners = cleanerRepository.findAllWithVehicle();

        List<String> allSlots = new ArrayList<>();

        for (CleanerProfessional cleaner : cleaners) {

            List<Booking> bookings = bookingRepository.findBookingsForCleaner(
                    cleaner.getId(),
                    date
            );

            // helper to compute daily free windows
            List<String> cleanerFreeSlots = AvailabilityUtil.generateAvailableSlots(bookings);

            allSlots.addAll(cleanerFreeSlots);
        }

        // Final output should be UNIQUE sorted time slots
        List<String> uniqueSorted = allSlots.stream()
                .distinct()
                .sorted()
                .toList();

        return new AvailabilityResponse(
                List.of(),         // no cleaners returned in this mode
                uniqueSorted
        );
    }

    private AvailabilityResponse getCleanersForRequestedSlot(AvailabilityRequest req) {

        LocalTime end = req.startTime().plusMinutes(req.durationMinutes());
        TimeWindow window = new TimeWindow(req.startTime(), end);

        List<CleanerProfessional> all = cleanerRepository.findAllWithVehicle();

        List<Long> available = all.stream()
                .filter(c -> cleanerRepository.isCleanerAvailable(
                        c.getId(),
                        req.date(),
                        window.start(),
                        window.end()
                ))
                .map(CleanerProfessional::getId)
                .toList();

        return new AvailabilityResponse(available, List.of());
    }
}