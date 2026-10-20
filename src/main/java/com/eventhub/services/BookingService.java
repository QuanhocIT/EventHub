package com.eventhub.services;

import com.eventhub.dto.request.CreateBookingRequest;
import com.eventhub.dto.response.BookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request, String currentUsername);

    Page<BookingResponse> getMyBookings(String currentUsername, Pageable pageable);

    BookingResponse getBookingByCode(String bookingCode, String currentUsername, boolean isAdminOrOrganizer);

    BookingResponse cancelBooking(String bookingCode, String currentUsername, boolean isAdmin);
}
