package com.eventhub.controllers;

import com.eventhub.common.ApiResponse;
import com.eventhub.dto.request.CreateVenueRequest;
import com.eventhub.dto.response.VenueResponse;
import com.eventhub.services.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/venues")
@RequiredArgsConstructor
@Tag(name = "Venues", description = "APIs for managing event venues and seating layouts")
public class VenueController {

    private final VenueService venueService;

    @Operation(summary = "Get list of all venues", description = "Public endpoint")
    @GetMapping
    public ResponseEntity<ApiResponse<List<VenueResponse>>> getAllVenues() {
        List<VenueResponse> venues = venueService.getAllVenues();
        return ResponseEntity.ok(ApiResponse.success("Venues retrieved successfully", venues));
    }

    @Operation(summary = "Get venue details by ID", description = "Public endpoint")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VenueResponse>> getVenueById(@PathVariable Long id) {
        VenueResponse venue = venueService.getVenueById(id);
        return ResponseEntity.ok(ApiResponse.success(venue));
    }

    @Operation(summary = "Create a new venue with auto-generated seats", description = "Requires ORGANIZER or ADMIN role", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<VenueResponse>> createVenue(@Valid @RequestBody CreateVenueRequest request) {
        VenueResponse createdVenue = venueService.createVenue(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Venue created successfully", createdVenue));
    }

    @Operation(summary = "Configure custom physical seating layout for a venue", description = "Requires ORGANIZER or ADMIN role", security = @SecurityRequirement(name = "BearerAuth"))
    @PutMapping("/{id}/layout")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<VenueResponse>> configureLayout(
            @PathVariable Long id,
            @Valid @RequestBody com.eventhub.dto.request.ConfigureVenueLayoutRequest request
    ) {
        VenueResponse response = venueService.configureVenueLayout(id, request);
        return ResponseEntity.ok(ApiResponse.success("Venue seating layout updated successfully", response));
    }

    @Operation(summary = "Delete venue by ID", description = "Requires ADMIN role", security = @SecurityRequirement(name = "BearerAuth"))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVenue(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return ResponseEntity.ok(ApiResponse.success("Venue deleted successfully", null));
    }
}
