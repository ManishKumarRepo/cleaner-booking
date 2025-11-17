package com.cleaning.booking.dto;

/**
 * Request payload for creating a new vehicle.
 *
 * @param name The display name of the vehicle (e.g., "Van A").
 */
public record VehicleCreateRequest(String name) {}
