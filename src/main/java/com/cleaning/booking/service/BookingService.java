package com.cleaning.booking.service;

import com.cleaning.booking.dto.BookingRequest;
import com.cleaning.booking.dto.BookingResponse;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request);

    BookingResponse updateBooking(Long bookingId, BookingRequest request);
}