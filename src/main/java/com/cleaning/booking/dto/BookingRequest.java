package com.cleaning.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request payload for creating a new booking.
 */
@Schema(description = "Booking creation request")
public record BookingRequest(

        @NotNull(message = "Date is required")
        @Schema(description = "Date of the booking", example = "2025-11-17")
        LocalDate date,

        @NotNull(message = "Start time is required")
        @Schema(description = "Start time (must be between 08:00-22:00)", example = "10:00")
        LocalTime startTime,

        @Schema(description = "Service duration in minutes (120 or 240)", example = "120")
        @Min(value = 120, message = "Duration must be 120 or 240 minutes")
        @Max(value = 240, message = "Duration must be 120 or 240 minutes")
        int durationMinutes,

        @Schema(description = "Number of cleaners required (1-3)")
        @Min(1) @Max(3)
        int cleanerCount

) {}
