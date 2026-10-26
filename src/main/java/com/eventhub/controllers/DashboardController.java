package com.eventhub.controllers;

import com.eventhub.common.ApiResponse;
import com.eventhub.dto.response.AdminDashboardResponse;
import com.eventhub.dto.response.EventAnalyticsResponse;
import com.eventhub.dto.response.OrganizerDashboardResponse;
import com.eventhub.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Tag(name = "Dashboard & Analytics", description = "Dashboard analytics and revenue reporting APIs")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get organizer dashboard statistics (Organizer / Admin)")
    @GetMapping("/organizer")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrganizerDashboardResponse>> getOrganizerDashboard(Principal principal) {
        OrganizerDashboardResponse response = dashboardService.getOrganizerDashboard(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get detailed analytics for a specific event (Organizer / Admin)")
    @GetMapping("/organizer/events/{eventId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EventAnalyticsResponse>> getEventAnalytics(
            @PathVariable Long eventId,
            Principal principal
    ) {
        EventAnalyticsResponse response = dashboardService.getEventAnalytics(eventId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Export event analytics report as CSV (Organizer / Admin)")
    @GetMapping("/organizer/events/{eventId}/export-report")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<byte[]> exportEventReport(
            @PathVariable Long eventId,
            Principal principal
    ) {
        byte[] csvData = dashboardService.exportEventReportCsv(eventId, principal.getName());
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"event_report_" + eventId + ".csv\"")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    @Operation(summary = "Get platform-wide admin dashboard statistics (Admin only)")
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard() {
        AdminDashboardResponse response = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
