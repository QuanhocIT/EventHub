package com.eventhub.controllers;

import com.eventhub.common.ApiResponse;
import com.eventhub.common.PageResponse;
import com.eventhub.dto.response.CheckInResponse;
import com.eventhub.dto.response.TicketResponse;
import com.eventhub.services.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Tag(name = "Tickets", description = "E-Ticket management and QR gate check-in APIs")
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "Get current user's purchased tickets with QR codes")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<TicketResponse>>> getMyTickets(
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

        Page<TicketResponse> ticketPage = ticketService.getMyTickets(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(ticketPage)));
    }

    @Operation(summary = "Get ticket details by ticket code")
    @GetMapping("/{ticketCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketByCode(
            @PathVariable String ticketCode,
            Principal principal,
            Authentication authentication
    ) {
        boolean isStaffOrAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF") || a.getAuthority().equals("ROLE_ADMIN"));

        TicketResponse response = ticketService.getTicketByCode(ticketCode, principal.getName(), isStaffOrAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Gate check-in for ticket (Staff or Admin only)")
    @PostMapping("/{ticketCode}/check-in")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<CheckInResponse>> checkInTicket(
            @PathVariable String ticketCode,
            Principal principal
    ) {
        CheckInResponse response = ticketService.checkInTicket(ticketCode, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Ticket checked in successfully", response));
    }

    @Operation(summary = "Pre-verify ticket at gate without marking as checked-in")
    @GetMapping("/{ticketCode}/verify")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'ORGANIZER')")
    public ResponseEntity<ApiResponse<CheckInResponse>> verifyTicket(
            @PathVariable String ticketCode,
            Principal principal
    ) {
        CheckInResponse response = ticketService.verifyTicket(ticketCode, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get real-time check-in gate dashboard statistics for event")
    @GetMapping("/events/{eventId}/gate-stats")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'ORGANIZER')")
    public ResponseEntity<ApiResponse<com.eventhub.dto.response.GateStatsResponse>> getGateStats(
            @PathVariable Long eventId
    ) {
        com.eventhub.dto.response.GateStatsResponse response = ticketService.getGateStats(eventId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
