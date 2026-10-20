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
public class OrganizerDashboardResponse {
    private Long organizerId;
    private long totalEvents;
    private long publishedEvents;
    private BigDecimal totalRevenue;
    private long totalTicketsIssued;
    private long totalTicketsCheckedIn;
    private double checkInRate;
}
