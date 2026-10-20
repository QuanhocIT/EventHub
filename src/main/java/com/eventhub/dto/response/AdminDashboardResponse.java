package com.eventhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalCustomers;
    private long totalOrganizers;
    private long totalStaff;
    private long totalEvents;
    private long publishedEvents;
    private BigDecimal totalRevenue;
    private long totalBookings;
    private long confirmedBookings;
    private long totalTickets;
}
