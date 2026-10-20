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
public class EventAnalyticsResponse {
    private Long eventId;
    private String eventTitle;
    private BigDecimal totalRevenue;
    private long totalTickets;
    private long checkedInTickets;
    private double checkInRate;
}
