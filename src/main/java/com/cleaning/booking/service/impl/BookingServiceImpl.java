package com.cleaning.booking.service.impl;

import com.cleaning.booking.dto.BookingRequest;
import com.cleaning.booking.dto.BookingResponse;
import com.cleaning.booking.entity.Booking;
import com.cleaning.booking.entity.CleanerProfessional;
import com.cleaning.booking.exception.*;
import com.cleaning.booking.repository.BookingRepository;
import com.cleaning.booking.repository.CleanerRepository;
import com.cleaning.booking.service.BookingService;
import com.cleaning.booking.util.TimeWindow;
import com.cleaning.booking.util.WorkHoursValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final CleanerRepository cleanerRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking: {}", request);

        validateRequest(request);

        LocalTime endTime = request.startTime().plusMinutes(request.durationMinutes());
        TimeWindow window = new TimeWindow(request.startTime(), endTime);

        // Get all cleaners sorted by vehicle grouping.
        List<CleanerProfessional> allCleaners = cleanerRepository.findAllWithVehicle();

        // Filter cleaners by availability
        List<CleanerProfessional> available = allCleaners.stream()
                .filter(c -> cleanerRepository.isCleanerAvailable(
                        c.getId(),
                        request.date(),
                        window.start(),
                        window.end()
                ))
                .toList();

        if (available.size() < request.cleanerCount()) {
            throw new BadRequestException("Not enough cleaners available for this slot.");
        }

        // Pick cleaners from SAME vehicle
        List<CleanerProfessional> chosen = pickCleanersFromSameVehicle(available, request.cleanerCount());

        if (chosen.isEmpty()) {
            throw new BadRequestException("No vehicle has enough available cleaners.");
        }

        // PESSIMISTIC LOCK BEFORE COMMITTING
        List<Long> ids = chosen.stream().map(CleanerProfessional::getId).toList();
        List<CleanerProfessional> lockedCleaners = cleanerRepository.lockCleanersForUpdate(ids);

        log.info("Locked cleaners for update: {}", ids);

        // Double-check overlap under lock (race condition safety)
        for (CleanerProfessional locked : lockedCleaners) {
            boolean overlap = bookingRepository.hasOverlap(
                    locked.getId(),
                    request.date(),
                    window.start().minusMinutes(30),
                    window.end().plusMinutes(30)
            );
            if (overlap) {
                log.warn("Cleaner {} has conflict within this TimeWindow [{}-{}]", locked.getId(), window.start(), window.end());
                throw new OverlapException(
                        "Cleaner "+locked.getId()+" has conflict within this TimeWindow ["+window.start()+"-"+window.end()+"]."
                );
            }
        }

        // Create booking for each cleaner
        List<Long> createdIds = new ArrayList<>();

        for (CleanerProfessional cleaner : lockedCleaners) {

            Booking b = Booking.builder()
                    .cleaner(cleaner)
                    .date(request.date())
                    .startTime(window.start())
                    .endTime(window.end())
                    .build();

            Booking saved = bookingRepository.save(b);
            createdIds.add(saved.getId());
        }

        log.info("Booking created successfully for cleaners: {} {}", ids, createdIds);

        return new BookingResponse(
                createdIds.get(0),
                request.date(),
                request.startTime(),
                endTime,
                ids
        );
    }

    @Override
    @Transactional
    public BookingResponse updateBooking(Long bookingId, BookingRequest request) {
        log.info("Updating booking {} with payload {}", bookingId, request);

        validateRequest(request);

        Booking existing = bookingRepository.lockBookingForUpdate(bookingId);
        if (existing == null) throw new EntityNotFoundException("Booking not found");

        // Remove previous booking and re-create new one
        bookingRepository.delete(existing);

        return createBooking(request);
    }

    /** Validate working hours, Friday rules, business constraints */
    private void validateRequest(BookingRequest req) {

        if (!WorkHoursValidator.isWorkingDay(req.date())) {
            throw new BadRequestException("Friday is not a working day.");
        }

        if (!WorkHoursValidator.isValidStartTime(req.startTime())) {
            throw new BadRequestException("Start time must be >= 08:00");
        }

        LocalTime end = req.startTime().plusMinutes(req.durationMinutes());

        if (!WorkHoursValidator.isValidEndTime(end)) {
            throw new BadRequestException("Booking must end before 22:00");
        }

        if (!WorkHoursValidator.isValidDuration(req.durationMinutes())) {
            throw new BadRequestException("Duration must be 120 or 240 minutes.");
        }
    }

    /** Find N cleaners from same vehicle */
    private List<CleanerProfessional> pickCleanersFromSameVehicle(
            List<CleanerProfessional> cleaners,
            int count
    ) {
        return cleaners.stream()
                .collect(Collectors.groupingBy(c -> c.getVehicle().getId()))
                .values().stream()
                .filter(list -> list.size() >= count)
                .map(list -> list.subList(0, count))
                .findFirst()
                .orElse(List.of());
    }
}