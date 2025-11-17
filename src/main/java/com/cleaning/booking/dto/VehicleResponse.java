package com.cleaning.booking.dto;

import java.util.List;

/**
 * Response payload representing a vehicle along with the cleaners assigned to it.
 *
 * @param id Unique identifier of the vehicle.
 * @param name Display name of the vehicle.
 * @param cleaners List of cleaners currently assigned to the vehicle.
 */
public record VehicleResponse(
        Long id,
        String name,
        List<CleanerResponse> cleaners
) {}

