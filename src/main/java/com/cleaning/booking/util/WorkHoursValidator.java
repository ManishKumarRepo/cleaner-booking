package com.cleaning.booking.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Utility class for validating work hours and business rules.
 */
public final class WorkHoursValidator {

    private static final LocalTime START = LocalTime.of(8, 0);
    private static final LocalTime END = LocalTime.of(22, 0);

    private WorkHoursValidator() {}

    /** Friday is non-working day */
    public static boolean isWorkingDay(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.FRIDAY;
    }

    /** Booking start time must be >= 08:00 */
    public static boolean isValidStartTime(LocalTime time) {
        return !time.isBefore(START);
    }

    /** Booking must end before 22:00 */
    public static boolean isValidEndTime(LocalTime time) {
        return !time.isAfter(END);
    }

    /** Duration must be 2 or 4 hours */
    public static boolean isValidDuration(int durationMinutes) {
        return durationMinutes == 120 || durationMinutes == 240;
    }
}