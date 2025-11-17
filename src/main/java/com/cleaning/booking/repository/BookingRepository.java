package com.cleaning.booking.repository;

import com.cleaning.booking.entity.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Repository for managing Booking data.
 * Includes overlap detection queries and entity-graph optimizations.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Fetch bookings for a cleaner on a specific date (prevents lazy loading issues).
     */
    @EntityGraph(attributePaths = {"cleaner", "cleaner.vehicle"})
    @Query("""
                SELECT b FROM Booking b
                WHERE b.cleaner.id = :cleanerId
                  AND b.date = :date
            """)
    List<Booking> findBookingsForCleaner(Long cleanerId, LocalDate date);

    /**
     * Check whether overlapping bookings exist for a cleaner.
     */
    @Query("""
                SELECT COUNT(b) > 0
                FROM Booking b
                WHERE b.cleaner.id = :cleanerId
                  AND b.date = :date
                  AND (
                       (b.startTime < :endTime AND b.endTime > :startTime)
                  )
            """)
    boolean hasOverlap(Long cleanerId, LocalDate date, LocalTime startTime, LocalTime endTime);

/*
    */
/**
     * Check whether overlapping bookings exist for a cleaner.
     *//*

    @Query("""
    SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
    FROM Booking b
    WHERE b.cleaner.id = :cleanerId
      AND b.date = :date
      AND (
            (:startTime < b.endTime + INTERVAL 30 MINUTE)
         AND (:endTime   > b.startTime - INTERVAL 30 MINUTE)
      )
""")
    boolean hasOverlapWithBreak(
            Long cleanerId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    );
*/


    /**
     * Pessimistic locking on an individual booking when updating.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Booking lockBookingForUpdate(Long id);
}