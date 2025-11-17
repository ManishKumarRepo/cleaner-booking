package com.cleaning.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Response payload returned after booking creation.
 */
@Schema(description = "Booking response")
public record BookingResponse(

        @Schema(description = "Booking ID", example = "101")
        Long bookingId,

        @Schema(description = "Date of booking", example = "2025-11-17")
        LocalDate date,

        @Schema(description = "Start time", example = "10:00")
        LocalTime startTime,

        @Schema(description = "End time", example = "12:00")
        LocalTime endTime,

        @Schema(description = "List of assigned cleaner IDs")
        List<Long> assignedCleaners

) {}