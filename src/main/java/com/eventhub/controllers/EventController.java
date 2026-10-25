package com.eventhub.controllers;

import com.eventhub.common.ApiResponse;
import com.eventhub.common.PageResponse;
import com.eventhub.dto.request.CreateEventRequest;
import com.eventhub.dto.request.UpdateEventRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.enums.EventStatus;
import com.eventhub.services.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.eventhub.enums.UserRole;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.eventhub.aspect.Auditable;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "APIs for managing events (sample module)")
public class EventController {

    private final EventService eventService;

    @Operation(summary = "Get list of events (paginated & searchable with advanced filters)", description = "Public endpoint to retrieve events")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> getAllEvents(
            @Parameter(description = "Keyword to filter by title, description or location")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filter by event status")
            @RequestParam(required = false) EventStatus status,
            @Parameter(description = "Filter by minimum ticket price")
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @Parameter(description = "Filter by maximum ticket price")
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @Parameter(description = "Filter events starting on or after (ISO-8601)")
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @Parameter(description = "Filter events ending on or before (ISO-8601)")
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate,
            @Parameter(description = "Filter by location (e.g. Hanoi, TP.HCM)")
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<EventResponse> events = eventService.getAllEvents(
                keyword, categoryId, status, minPrice, maxPrice, startDate, endDate, location, pageable);
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", events));
    }

    @Operation(summary = "Get event by ID", description = "Public endpoint to retrieve details of a specific event")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Long id) {
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @Operation(summary = "Create a new event", description = "Requires ORGANIZER or ADMIN role", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            java.security.Principal principal
    ) {
        EventResponse createdEvent = eventService.createEvent(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event created successfully", createdEvent));
    }

    @Operation(summary = "Update an existing event", description = "Requires Owner or ADMIN role", security = @SecurityRequirement(name = "BearerAuth"))
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request,
            Authentication authentication
    ) {
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(UserRole.ROLE_ADMIN.name()));
        EventResponse updatedEvent = eventService.updateEvent(id, request, currentUsername, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", updatedEvent));
    }

    @Operation(summary = "Update event status (Publish, Draft, Cancel)", description = "Requires Owner or ADMIN role", security = @SecurityRequirement(name = "BearerAuth"))
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @Auditable(action = "EVENT_STATUS_UPDATE", entityType = "Event")
    public ResponseEntity<ApiResponse<EventResponse>> updateEventStatus(
            @PathVariable Long id,
            @RequestParam EventStatus status,
            Authentication authentication
    ) {
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(UserRole.ROLE_ADMIN.name()));
        EventResponse updatedEvent = eventService.updateEventStatus(id, status, currentUsername, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Event status updated successfully", updatedEvent));
    }

    @Operation(summary = "Delete an event by ID", description = "Requires Owner or ADMIN role", security = @SecurityRequirement(name = "BearerAuth"))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(UserRole.ROLE_ADMIN.name()));
        eventService.deleteEvent(id, currentUsername, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully", null));
    }

    @Operation(summary = "Get seating map with status for a specific event", description = "Public endpoint")
    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse<java.util.List<com.eventhub.dto.response.EventSeatResponse>>> getEventSeats(@PathVariable Long id) {
        java.util.List<com.eventhub.dto.response.EventSeatResponse> seats = eventService.getEventSeats(id);
        return ResponseEntity.ok(ApiResponse.success("Seating map retrieved successfully", seats));
    }

    @Operation(summary = "Assign a venue to an event and generate event seats", description = "Requires Owner or ADMIN role", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping("/{id}/assign-venue")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<java.util.List<com.eventhub.dto.response.EventSeatResponse>>> assignVenue(
            @PathVariable Long id,
            @RequestParam Long venueId,
            @RequestParam(required = false) java.math.BigDecimal basePrice,
            Authentication authentication
    ) {
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(UserRole.ROLE_ADMIN.name()));
        java.util.List<com.eventhub.dto.response.EventSeatResponse> seats =
                eventService.assignVenueToEvent(id, venueId, basePrice, currentUsername, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Venue assigned and seats generated successfully", seats));
    }

    @Operation(summary = "Configure visual seat map layout and pricing for an event", description = "Requires Owner or ADMIN role", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping("/{id}/seat-map")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<java.util.List<com.eventhub.dto.response.EventSeatResponse>>> configureSeatMap(
            @PathVariable Long id,
            @Valid @RequestBody com.eventhub.dto.request.ConfigureEventSeatsRequest request,
            Authentication authentication
    ) {
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(UserRole.ROLE_ADMIN.name()));
        java.util.List<com.eventhub.dto.response.EventSeatResponse> seats =
                eventService.configureEventSeats(id, request, currentUsername, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Event seat map configured successfully", seats));
    }

    @Operation(summary = "Batch update event seats (price, status, zone)", description = "Requires Owner or ADMIN role", security = @SecurityRequirement(name = "BearerAuth"))
    @PutMapping("/{id}/seats/batch-update")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<java.util.List<com.eventhub.dto.response.EventSeatResponse>>> batchUpdateSeats(
            @PathVariable Long id,
            @Valid @RequestBody com.eventhub.dto.request.BatchUpdateEventSeatsRequest request,
            Authentication authentication
    ) {
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(UserRole.ROLE_ADMIN.name()));
        java.util.List<com.eventhub.dto.response.EventSeatResponse> seats =
                eventService.batchUpdateEventSeats(id, request, currentUsername, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Event seats updated successfully", seats));
    }
}
