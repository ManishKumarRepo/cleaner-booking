package com.cleaning.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a single cleaner's booking appointment.
 * Each cleaner receives an entry for the booking.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Date of the appointment */
    @Column(nullable = false)
    private LocalDate date;

    /** Start time for the appointment */
    @Column(nullable = false)
    private LocalTime startTime;

    /** End time of the appointment */
    @Column(nullable = false)
    private LocalTime endTime;

    /**
     * The cleaner assigned to this booking.
     * A multi-cleaner job creates multiple Booking rows (one per cleaner).
     *
     * Uses LAZY loading and pessimistic locking is applied at repository level.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleaner_id", nullable = false)
    private CleanerProfessional cleaner;
}