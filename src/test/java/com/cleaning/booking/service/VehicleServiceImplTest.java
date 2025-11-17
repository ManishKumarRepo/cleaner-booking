package com.cleaning.booking.service;

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

import com.cleaning.booking.service.impl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleServiceImplTest {

    private VehicleRepository vehicleRepository;
    private CleanerRepository cleanerRepository;
    private VehicleServiceImpl service;

    @BeforeEach
    void setup() {
        vehicleRepository = mock(VehicleRepository.class);
        cleanerRepository = mock(CleanerRepository.class);
        service = new VehicleServiceImpl(vehicleRepository, cleanerRepository);
    }

    // Utility builders --------------------------------------------------------

    private Vehicle vehicle(long id, String name) {
        Vehicle v = new Vehicle();
        v.setId(id);
        v.setName(name);
        v.setCleaners(new ArrayList<>());
        return v;
    }

    private CleanerProfessional cleaner(long id, String name, Vehicle vehicle) {
        CleanerProfessional c = new CleanerProfessional();
        c.setId(id);
        c.setName(name);
        c.setVehicle(vehicle);
        return c;
    }

    // -------------------------------------------------------------------------
    // 1. Create Vehicle
    // -------------------------------------------------------------------------
    @Test
    void createVehicle_success() {

        VehicleCreateRequest req = new VehicleCreateRequest("Van A");

        Vehicle savedVehicle = vehicle(1L, "Van A");

        when(vehicleRepository.save(any(Vehicle.class)))
                .thenAnswer(invocation -> {
                    Vehicle v = invocation.getArgument(0);
                    v.setId(1L);
                    return v;
                });

        VehicleResponse response = service.createVehicle(req);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Van A", response.name());
        assertTrue(response.cleaners().isEmpty());

        // Ensure repository save called
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    // -------------------------------------------------------------------------
    // 2. Add Cleaner to Vehicle — Success
    // -------------------------------------------------------------------------
    @Test
    void addCleanerToVehicle_success() {

        Vehicle vehicle = vehicle(1L, "Car X");
        CleanerCreateRequest req = new CleanerCreateRequest("John");

        when(vehicleRepository.findByIdWithCleaners(1L))
                .thenReturn(Optional.of(vehicle));

        when(cleanerRepository.save(any(CleanerProfessional.class)))
                .thenAnswer(invocation -> {
                    CleanerProfessional c = invocation.getArgument(0);
                    c.setId(100L);
                    return c;
                });

        CleanerResponse res = service.addCleanerToVehicle(1L, req);

        assertEquals(100L, res.id());
        assertEquals("John", res.name());
        assertEquals(1L, res.vehicleId());

        // Cleaner should be associated to the vehicle
        assertEquals(1, vehicle.getCleaners().size() + 1);

        verify(cleanerRepository, times(1)).save(any(CleanerProfessional.class));
    }

    // -------------------------------------------------------------------------
    // 3. Add Cleaner — Vehicle Not Found
    // -------------------------------------------------------------------------
    @Test
    void addCleanerToVehicle_vehicleNotFound_throwsException() {

        CleanerCreateRequest req = new CleanerCreateRequest("Emily");

        when(vehicleRepository.findByIdWithCleaners(1L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.addCleanerToVehicle(1L, req));

        verify(cleanerRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // 4. Add Cleaner — Max 5 Limit
    // -------------------------------------------------------------------------
    @Test
    void addCleanerToVehicle_rejectWhenMoreThanFive() {

        Vehicle vehicle = vehicle(1L, "Car Max");

        // Fill vehicle with 5 cleaners
        for (int i = 0; i < 5; i++) {
            CleanerProfessional c = cleaner(i + 1L, "C" + i, vehicle);
            vehicle.getCleaners().add(c);
        }

        CleanerCreateRequest req = new CleanerCreateRequest("John");

        when(vehicleRepository.findByIdWithCleaners(1L))
                .thenReturn(Optional.of(vehicle));

        assertThrows(BadRequestException.class,
                () -> service.addCleanerToVehicle(1L, req));

        verify(cleanerRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // 5. getAllVehicles()
    // -------------------------------------------------------------------------
    @Test
    void getAllVehicles_success() {

        Vehicle v1 = vehicle(1L, "Van A");
        Vehicle v2 = vehicle(2L, "Van B");

        CleanerProfessional c1 = cleaner(101L, "Mike", v1);
        CleanerProfessional c2 = cleaner(102L, "Ana", v2);

        v1.getCleaners().add(c1);
        v2.getCleaners().add(c2);

        when(vehicleRepository.findAllWithCleaners())
                .thenReturn(List.of(v1, v2));

        List<VehicleResponse> responses = service.getAllVehicles();

        assertEquals(2, responses.size());
        assertEquals("Van A", responses.get(0).name());
        assertEquals(1, responses.get(0).cleaners().size());
        assertEquals("Mike", responses.get(0).cleaners().get(0).name());

        verify(vehicleRepository, times(1)).findAllWithCleaners();
    }

    // -------------------------------------------------------------------------
    // 6. getVehicle()
    // -------------------------------------------------------------------------
    @Test
    void getVehicle_success() {

        Vehicle v = vehicle(10L, "Shuttle");
        CleanerProfessional c1 = cleaner(201L, "Jess", v);
        v.getCleaners().add(c1);

        when(vehicleRepository.findByIdWithCleaners(10L))
                .thenReturn(Optional.of(v));

        VehicleResponse res = service.getVehicle(10L);

        assertEquals(10L, res.id());
        assertEquals("Shuttle", res.name());
        assertEquals(1, res.cleaners().size());
        assertEquals("Jess", res.cleaners().get(0).name());
    }

    // -------------------------------------------------------------------------
    // 7. getVehicle — Not Found
    // -------------------------------------------------------------------------
    @Test
    void getVehicle_vehicleNotFound_throwsException() {

        when(vehicleRepository.findByIdWithCleaners(5L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.getVehicle(5L));
    }
}
