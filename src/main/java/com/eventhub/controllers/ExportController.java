package com.eventhub.controllers;

import com.eventhub.services.ExcelExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Data Export", description = "APIs for exporting Excel spreadsheets (attendees, revenue statements)")
public class ExportController {

    private final ExcelExportService excelExportService;

    @Operation(summary = "Export event attendee list as Excel", description = "Generates a styled .xlsx file containing verified attendees, ticket codes, seat positions, and check-in times")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @GetMapping("/events/{eventId}/attendees")
    public ResponseEntity<byte[]> exportEventAttendees(
            @PathVariable Long eventId,
            Principal principal,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        byte[] excelBytes = excelExportService.exportEventAttendees(eventId, principal.getName(), isAdmin);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attendees_event_" + eventId + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @Operation(summary = "Export organizer revenue and financial report as Excel", description = "Generates a styled .xlsx report with annual revenue, monthly performance breakdown, and voucher metrics")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @GetMapping("/organizers/{organizerId}/revenue")
    public ResponseEntity<byte[]> exportOrganizerRevenueReport(
            @PathVariable Long organizerId,
            @Parameter(description = "Fiscal year (defaults to current year)")
            @RequestParam(required = false) Integer year,
            Principal principal,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        byte[] excelBytes = excelExportService.exportOrganizerRevenueReport(organizerId, year, principal.getName(), isAdmin);

        int exportYear = year != null ? year : java.time.LocalDateTime.now().getYear();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"revenue_organizer_" + organizerId + "_" + exportYear + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
