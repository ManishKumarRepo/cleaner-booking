package com.cleaning.booking.service;

import com.cleaning.booking.dto.CleanerCreateRequest;
import com.cleaning.booking.dto.CleanerResponse;
import com.cleaning.booking.dto.VehicleCreateRequest;
import com.cleaning.booking.dto.VehicleResponse;

import java.util.List;

public interface VehicleService {
    VehicleResponse createVehicle(VehicleCreateRequest request);

    CleanerResponse addCleanerToVehicle(Long vehicleId, CleanerCreateRequest request);

    List<VehicleResponse> getAllVehicles();

    VehicleResponse getVehicle(Long id);
}
