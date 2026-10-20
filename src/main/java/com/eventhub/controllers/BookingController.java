package com.eventhub.controllers;

import com.eventhub.common.ApiResponse;
import com.eventhub.common.PageResponse;
import com.eventhub.dto.request.CreateBookingRequest;
import com.eventhub.dto.response.BookingResponse;
import com.eventhub.services.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Tag(name = "Bookings", description = "Ticket booking management APIs")
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Create a new booking (Authenticated users)")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Principal principal
    ) {
        BookingResponse response = bookingService.createBooking(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", response));
    }

    @Operation(summary = "Get current user's booking history")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Principal principal
    ) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BookingResponse> bookingPage = bookingService.getMyBookings(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(bookingPage)));
    }

    @Operation(summary = "Get booking details by code")
    @GetMapping("/{bookingCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByCode(
            @PathVariable String bookingCode,
            Principal principal,
            Authentication authentication
    ) {
        boolean isAdminOrOrganizer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ORGANIZER"));

        BookingResponse response = bookingService.getBookingByCode(bookingCode, principal.getName(), isAdminOrOrganizer);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Cancel a pending booking")
    @PostMapping("/{bookingCode}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable String bookingCode,
            Principal principal,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        BookingResponse response = bookingService.cancelBooking(bookingCode, principal.getName(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", response));
    }
}
