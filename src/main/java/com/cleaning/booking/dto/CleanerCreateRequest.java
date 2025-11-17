package com.cleaning.booking.dto;

/**
 * Request payload for creating a cleaning professional and assigning them to a vehicle.
 *
 * @param name Full name of the cleaning professional.
 */
public record CleanerCreateRequest(String name) {}
