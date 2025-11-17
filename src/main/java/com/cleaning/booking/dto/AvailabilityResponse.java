package com.cleaning.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Generic response for cleaner availability queries.
 */
@Schema(description = "Availability response")
public record AvailabilityResponse(

        @Schema(description = "Available cleaner IDs for the requested time")
        List<Long> availableCleanerIds,

        @Schema(description = "List of available time windows for the day")
        List<String> availableTimeSlots

) {}