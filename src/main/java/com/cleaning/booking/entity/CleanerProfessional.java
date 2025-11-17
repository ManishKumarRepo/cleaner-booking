package com.cleaning.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Represents an individual cleaning professional.
 * Cleaners belong to vehicles and receive multiple bookings.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "cleaner_professional")
public class CleanerProfessional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cleaner full name */
    @Column(nullable = false)
    private String name;

    /**
     * Vehicle assignment: required.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    /**
     * Existing bookings for availability checks.
     */
    @OneToMany(mappedBy = "cleaner", fetch = FetchType.LAZY)
    private List<Booking> bookings;
}