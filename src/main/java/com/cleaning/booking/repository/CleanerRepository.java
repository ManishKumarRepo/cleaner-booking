package com.cleaning.booking.repository;

import com.cleaning.booking.entity.CleanerProfessional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Repository for handling cleaner-professional data and advanced queries.
 */
@Repository
public interface CleanerRepository extends JpaRepository<CleanerProfessional, Long> {

    /**
     * Load all cleaners with their vehicles (prevents N+1).
     */
    @EntityGraph(attributePaths = {"vehicle"})
    @Query("SELECT c FROM CleanerProfessional c")
    List<CleanerProfessional> findAllWithVehicle();

    /**
     * Fetch all cleaners that belong to a specific vehicle.
     */
    @EntityGraph(attributePaths = {"vehicle"})
    List<CleanerProfessional> findByVehicleId(Long vehicleId);

    /**
     * Pessimistic lock during booking creation to prevent concurrency issues.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CleanerProfessional c WHERE c.id IN :ids")
    List<CleanerProfessional> lockCleanersForUpdate(List<Long> ids);

    /**
     * Check whether a cleaner is available (no overlapping booking).
     */
    @Query("""
        SELECT COUNT(b) = 0
        FROM Booking b
        WHERE b.cleaner.id = :cleanerId
          AND b.date = :date
          AND (
               (b.startTime < :endTime AND b.endTime > :startTime)
          )
    """)
    boolean isCleanerAvailable(Long cleanerId, LocalDate date, LocalTime startTime, LocalTime endTime);
}