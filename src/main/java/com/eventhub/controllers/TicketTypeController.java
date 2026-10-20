package com.eventhub.controllers;

import com.eventhub.common.ApiResponse;
import com.eventhub.dto.request.CreateTicketTypeRequest;
import com.eventhub.dto.response.TicketTypeResponse;
import com.eventhub.services.TicketTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@Tag(name = "Ticket Types", description = "Ticket type management APIs for events")
@RestController
@RequestMapping("/api/v1/events/{eventId}/ticket-types")
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @Operation(summary = "Create ticket type for an event (Organizer or Admin)")
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TicketTypeResponse>> createTicketType(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateTicketTypeRequest request,
            Principal principal,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        TicketTypeResponse response = ticketTypeService.createTicketType(eventId, request, principal.getName(), isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket type created successfully", response));
    }

    @Operation(summary = "Get all active ticket types for an event")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketTypeResponse>>> getTicketTypesByEvent(@PathVariable Long eventId) {
        List<TicketTypeResponse> response = ticketTypeService.getTicketTypesByEvent(eventId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get specific ticket type by ID")
    @GetMapping("/{ticketTypeId}")
    public ResponseEntity<ApiResponse<TicketTypeResponse>> getTicketTypeById(
            @PathVariable Long eventId,
            @PathVariable Long ticketTypeId
    ) {
        TicketTypeResponse response = ticketTypeService.getTicketTypeById(eventId, ticketTypeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
