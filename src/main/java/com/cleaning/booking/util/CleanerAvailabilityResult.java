package com.cleaning.booking.util;

import java.util.List;

/**
 * Internal result container for cleaner availability checks.
 */
public record CleanerAvailabilityResult(
        Long cleanerId,
        boolean available,
        List<String> dailyAvailableSlots
) { }
