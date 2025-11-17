package com.cleaning.booking.util;

import java.time.LocalTime;

/**
 * Immutable representation of a start-end time window.
 */
public record TimeWindow(LocalTime start, LocalTime end) {

    /** Checks whether two time windows overlap. */
    public boolean overlaps(TimeWindow other) {
        return this.start().isBefore(other.end()) &&
                this.end().isAfter(other.start());
    }

    /** Enforces 30-minute break between appointments. */
    public boolean violatesBreakWith(TimeWindow other) {
        LocalTime endWithBreak = this.end().plusMinutes(30);
        LocalTime startWithBreak = other.start().minusMinutes(30);

        return endWithBreak.isAfter(other.start()) ||
                this.start().isBefore(startWithBreak);
    }
}