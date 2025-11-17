package com.cleaning.booking.dto;

/**
 * Response payload representing a cleaning professional.
 *
 * @param id Unique identifier of the cleaner.
 * @param name Full name of the cleaner.
 * @param vehicleId Identifier of the vehicle the cleaner belongs to.
 */
public record CleanerResponse(
        Long id,
        String name,
        Long vehicleId
) {}


