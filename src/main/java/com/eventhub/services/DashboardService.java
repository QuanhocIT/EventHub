package com.eventhub.services;

import com.eventhub.dto.response.AdminDashboardResponse;
import com.eventhub.dto.response.EventAnalyticsResponse;
import com.eventhub.dto.response.OrganizerDashboardResponse;

public interface DashboardService {

    OrganizerDashboardResponse getOrganizerDashboard(String username);

    EventAnalyticsResponse getEventAnalytics(Long eventId, String username);

    AdminDashboardResponse getAdminDashboard();
}
