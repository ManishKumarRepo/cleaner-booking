package com.cleaning.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request object for checking availability.
 */
@Schema(description = "Availability check request")
public record AvailabilityRequest(

        @NotNull(message = "Date is required")
        @Schema(description = "Date to check availability", example = "2025-11-17")
        LocalDate date,

        @Schema(description = "Start time (optional for daily availability query)", example = "10:00")
        LocalTime startTime,

        @Schema(description = "Service duration (optional)", example = "120")
        Integer durationMinutes

) {}