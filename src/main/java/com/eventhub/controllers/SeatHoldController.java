package com.eventhub.controllers;

import com.eventhub.common.ApiResponse;
import com.eventhub.dto.request.SeatHoldRequest;
import com.eventhub.dto.response.SeatHoldResponse;
import com.eventhub.services.SeatHoldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Tag(name = "Seat Holds", description = "Temporary seat hold APIs with Redis TTL")
@RestController
@RequestMapping("/api/v1/seat-holds")
@RequiredArgsConstructor
public class SeatHoldController {

    private final SeatHoldService seatHoldService;

    @Operation(summary = "Hold seats temporarily (5 minutes TTL)")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SeatHoldResponse>> holdSeats(
            @Valid @RequestBody SeatHoldRequest request,
            Principal principal
    ) {
        SeatHoldResponse response = seatHoldService.holdSeats(request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Seats held successfully for 5 minutes", response));
    }

    @Operation(summary = "Release held seats proactively")
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> releaseSeats(
            @Valid @RequestBody SeatHoldRequest request,
            Principal principal
    ) {
        seatHoldService.releaseSeats(request.getEventId(), request.getEventSeatIds(), principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Seats released successfully", null));
    }
}
