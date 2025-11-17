package com.cleaning.booking.repository;

import com.cleaning.booking.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Vehicle entities.
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Query("SELECT v FROM Vehicle v LEFT JOIN FETCH v.cleaners")
    List<Vehicle> findAllWithCleaners();

    @Query("SELECT v FROM Vehicle v LEFT JOIN FETCH v.cleaners WHERE v.id = :id")
    Optional<Vehicle> findByIdWithCleaners(Long id);
}
