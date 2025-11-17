package com.cleaning.booking.service.impl;

import com.cleaning.booking.dto.CleanerCreateRequest;
import com.cleaning.booking.dto.CleanerResponse;
import com.cleaning.booking.dto.VehicleCreateRequest;
import com.cleaning.booking.dto.VehicleResponse;
import com.cleaning.booking.entity.CleanerProfessional;
import com.cleaning.booking.entity.Vehicle;
import com.cleaning.booking.exception.BadRequestException;
import com.cleaning.booking.exception.EntityNotFoundException;
import com.cleaning.booking.repository.CleanerRepository;
import com.cleaning.booking.repository.VehicleRepository;
import com.cleaning.booking.service.VehicleService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer responsible for all business logic related to vehicles and cleaners.
 *
 * <p>This service supports:
 * <ul>
 *   <li>Creating vehicles</li>
 *   <li>Assigning cleaners to vehicles</li>
 *   <li>Enforcing vehicle capacity rules</li>
 *   <li>Fetching vehicles with their assigned cleaners</li>
 * </ul>
 *
 * <p>All operations are transactional to ensure consistency.</p>
 */
@Service
@Transactional
@Slf4j
public class VehicleServiceImpl implements VehicleService {
    private final VehicleRepository vehicleRepository;
    private final CleanerRepository cleanerRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, CleanerRepository cleanerRepository) {
        this.vehicleRepository = vehicleRepository;
        this.cleanerRepository = cleanerRepository;
    }

    /**
     * Creates a new vehicle in the system.
     *
     * @param request DTO containing the name of the vehicle.
     * @return A response DTO representing the stored vehicle.
     */
    @Override
    public VehicleResponse createVehicle(VehicleCreateRequest request) {
        log.info("Creating vehicle with name={}", request.name());

        Vehicle vehicle = new Vehicle();
        vehicle.setName(request.name());
        vehicleRepository.save(vehicle);

        log.debug("Vehicle created with id={}", vehicle.getId());
        return toVehicleResponse(vehicle);
    }

    /**
     * Adds a new cleaner to a given vehicle.
     *
     * @param vehicleId The ID of the vehicle to which the cleaner is added.
     * @param request DTO containing cleaner name data.
     * @return A response DTO representing the assigned cleaner.
     *
     * @throws EntityNotFoundException if the vehicle does not exist.
     * @throws BadRequestException if the vehicle already contains 5 cleaners.
     */
    public CleanerResponse addCleanerToVehicle(Long vehicleId, CleanerCreateRequest request) {

        log.info("Adding cleaner '{}' to vehicle id={}", request.name(), vehicleId);

        Vehicle vehicle = vehicleRepository.findByIdWithCleaners(vehicleId)
                .orElseThrow(() -> {
                    log.warn("Vehicle not found id={}", vehicleId);
                    return new EntityNotFoundException("Vehicle not found");
                });

        if (vehicle.getCleaners().size() >= 5) {
            log.warn("Vehicle id={} has reached maximum cleaner capacity", vehicleId);
            throw new BadRequestException("Vehicle already has 5 cleaners assigned.");
        }

        CleanerProfessional cleaner = new CleanerProfessional();
        cleaner.setName(request.name());
        cleaner.setVehicle(vehicle);

        cleanerRepository.save(cleaner);

        log.debug("Cleaner '{}' added with id={} to vehicle={}",
                request.name(), cleaner.getId(), vehicleId);

        return new CleanerResponse(cleaner.getId(), cleaner.getName(), vehicleId);
    }

    /**
     * Retrieves all vehicles along with their assigned cleaners.
     *
     * @return A list of vehicle response DTOs.
     */
    @Override
    public List<VehicleResponse> getAllVehicles() {
        log.info("Fetching list of all vehicles with assigned cleaners");
        return vehicleRepository.findAllWithCleaners().stream()
                .map(this::toVehicleResponse)
                .toList();
    }

    /**
     * Retrieves a single vehicle with all associated cleaners.
     *
     * @param id ID of the vehicle.
     * @return A fully populated vehicle response object.
     *
     * @throws EntityNotFoundException if no vehicle with the given ID exists.
     */
    public VehicleResponse getVehicle(Long id) {
        log.info("Fetching vehicle id={}", id);

        Vehicle vehicle = vehicleRepository.findByIdWithCleaners(id)
                .orElseThrow(() -> {
                    log.warn("Vehicle not found id={}", id);
                    return new EntityNotFoundException("Vehicle not found");
                });

        return toVehicleResponse(vehicle);
    }

    /**
     * Converts a Vehicle JPA entity into a response DTO.
     *
     * @param vehicle Entity to convert.
     * @return A DTO representing the vehicle and its cleaners.
     */
    private VehicleResponse toVehicleResponse(Vehicle vehicle) {
        if(vehicle.getCleaners() == null) {
            return new VehicleResponse(vehicle.getId(), vehicle.getName(), List.of());
        }
        List<CleanerResponse> cleaners = vehicle.getCleaners().stream()
                .map(c -> new CleanerResponse(c.getId(), c.getName(), vehicle.getId()))
                .toList();

        return new VehicleResponse(vehicle.getId(), vehicle.getName(), cleaners);
    }
}

