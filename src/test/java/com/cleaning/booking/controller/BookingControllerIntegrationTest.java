package com.cleaning.booking.controller;

import com.cleaning.booking.dto.BookingRequest;
import com.cleaning.booking.entity.CleanerProfessional;
import com.cleaning.booking.entity.Vehicle;
import com.cleaning.booking.repository.CleanerRepository;
import com.cleaning.booking.repository.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CleanerRepository cleanerRepository;

    @BeforeEach
    void setup() {

        // Clear previous data (important for @SpringBootTest)
        cleanerRepository.deleteAll();
        vehicleRepository.deleteAll();

        // Create vehicle
        Vehicle v = new Vehicle();
        v.setName("Van-01");
        Vehicle savedVehicle = vehicleRepository.save(v);

        // Create cleaner
        CleanerProfessional c = new CleanerProfessional();
        c.setName("John Cleaner");
        c.setVehicle(savedVehicle);
        cleanerRepository.save(c);
    }

    @Test
    void testCreateBooking_Success() throws Exception {
        BookingRequest request = new BookingRequest(LocalDate.now(), LocalTime.of(10,0), 120, 1);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").exists());
    }

    @Test
    void testCreateBooking_InvalidRequest() throws Exception {
        BookingRequest request = new BookingRequest(LocalDate.now(), LocalTime.of(5,0), 260, 6);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}