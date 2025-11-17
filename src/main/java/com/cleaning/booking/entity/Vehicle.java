package com.cleaning.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a vehicle assigned with 5 cleaning professionals.
 * Cleaners assigned to this vehicle must work together for multi-cleaner jobs.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "vehicle")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human readable vehicle name (e.g., Vehicle A) */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Cleaners assigned to this vehicle.
     * LAZY loaded to avoid unnecessary fetching.
     */
    @OneToMany(mappedBy = "vehicle", fetch = FetchType.LAZY)
    private List<CleanerProfessional> cleaners = new ArrayList<>();
}

