package com.cleaning.booking.service;

import com.cleaning.booking.dto.AvailabilityRequest;
import com.cleaning.booking.dto.AvailabilityResponse;

public interface AvailabilityService {

    AvailabilityResponse checkAvailability(AvailabilityRequest request);
}
