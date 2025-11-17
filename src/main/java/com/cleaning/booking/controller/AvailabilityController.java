package com.cleaning.booking.controller;

import com.cleaning.booking.dto.AvailabilityRequest;
import com.cleaning.booking.dto.AvailabilityResponse;
import com.cleaning.booking.service.AvailabilityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for availability queries.
 */
@Slf4j
@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Cleaner availability checking")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping
    @Operation(summary = "Check cleaner availability for a date or specific time slot")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @Valid @RequestBody AvailabilityRequest request
    ) {
        log.info("API: Checking availability for: {}", request);
        return ResponseEntity.ok(availabilityService.checkAvailability(request));
    }
}