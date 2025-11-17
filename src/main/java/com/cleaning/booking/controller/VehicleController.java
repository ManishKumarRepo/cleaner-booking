package com.cleaning.booking.controller;

import com.cleaning.booking.dto.CleanerCreateRequest;
import com.cleaning.booking.dto.CleanerResponse;
import com.cleaning.booking.dto.VehicleCreateRequest;
import com.cleaning.booking.dto.VehicleResponse;
import com.cleaning.booking.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller that exposes vehicle and cleaner management endpoints.
 *
 * <p>Supported operations include:
 * <ul>
 *   <li>Create vehicle</li>
 *   <li>Add cleaner to vehicle</li>
 *   <li>Fetch all vehicles with cleaners</li>
 *   <li>Fetch a single vehicle with cleaners</li>
 * </ul>
 *
 * <p>All responses follow standardized DTOs and are documented using Swagger/OpenAPI.</p>
 */
@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicles", description = "Vehicle and cleaner management API")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    /**
     * Creates a new vehicle.
     *
     * @param request Body containing vehicle creation details.
     * @return HTTP 201 with created vehicle data.
     */
    @PostMapping
    @Operation(summary = "Create a new vehicle")
    public ResponseEntity<VehicleResponse> createVehicle(
            @Valid @RequestBody VehicleCreateRequest request) {
        return ResponseEntity.status(201).body(vehicleService.createVehicle(request));
    }

    /**
     * Assigns a cleaner to a vehicle.
     *
     * @param vehicleId ID of the vehicle.
     * @param request Body containing cleaner name.
     * @return HTTP 201 with cleaner response.
     */
    @PostMapping("/{vehicleId}/cleaners")
    @Operation(summary = "Add a cleaner to a vehicle")
    public ResponseEntity<CleanerResponse> addCleaner(
            @PathVariable Long vehicleId,
            @Valid @RequestBody CleanerCreateRequest request) {

        return ResponseEntity.status(201)
                .body(vehicleService.addCleanerToVehicle(vehicleId, request));
    }

    /**
     * Retrieves all vehicles with assigned cleaners.
     *
     * @return List of vehicle responses.
     */
    @GetMapping
    @Operation(summary = "List all vehicles with assigned cleaners")
    public List<VehicleResponse> getVehicles() {
        return vehicleService.getAllVehicles();
    }

    /**
     * Retrieves a single vehicle with assigned cleaners.
     *
     * @param id Vehicle ID.
     * @return Vehicle response DTO.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Fetch a specific vehicle by ID")
    public VehicleResponse getVehicle(@PathVariable Long id) {
        return vehicleService.getVehicle(id);
    }
}
