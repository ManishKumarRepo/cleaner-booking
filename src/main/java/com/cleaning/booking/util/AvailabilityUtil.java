package com.cleaning.booking.util;

import com.cleaning.booking.entity.Booking;

import java.time.LocalTime;
import java.util.*;

/**
 * Helper class for calculating available time slots for cleaners.
 */
public final class AvailabilityUtil {

    private AvailabilityUtil() {}

    /**
     * Generates available 2-hour or 4-hour slots for a given cleaner.
     *
     * @param existing existing bookings for the date
     * @return list of available time windows
     */
    public static List<String> generateAvailableSlots(List<Booking> existing) {

        // working hours: 08:00–22:00
        LocalTime cursor = LocalTime.of(8, 0);
        LocalTime endOfDay = LocalTime.of(22, 0);

        List<String> available = new ArrayList<>();

        while (cursor.plusHours(2).isBefore(endOfDay.plusSeconds(1))) {

            TimeWindow twoHour = new TimeWindow(cursor, cursor.plusHours(2));
            TimeWindow fourHour = new TimeWindow(cursor, cursor.plusHours(4));

            boolean twoFree = isWindowFree(existing, twoHour);
            boolean fourFree = isWindowFree(existing, fourHour);

            if (twoFree) {
                available.add(twoHour.start() + " - " + twoHour.end());
            }
            if (fourFree) {
                available.add(fourHour.start() + " - " + fourHour.end());
            }

            cursor = cursor.plusMinutes(30);
        }

        return available;
    }

    /** Checks if window is conflict-free AND break-rule safe */
    public static boolean isWindowFree(List<Booking> bookings, TimeWindow requested) {
        for (Booking b : bookings) {
            TimeWindow existing = new TimeWindow(b.getStartTime(), b.getEndTime());
            if (existing.overlaps(requested) || existing.violatesBreakWith(requested)) {
                return false;
            }
        }
        return true;
    }
}