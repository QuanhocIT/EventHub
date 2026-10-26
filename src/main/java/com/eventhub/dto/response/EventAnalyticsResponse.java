package com.eventhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAnalyticsResponse {
    private Long eventId;
    private String eventTitle;
    private String venueName;
    private BigDecimal totalRevenue;
    private BigDecimal netRevenue;
    private BigDecimal discountTotal;
    private long totalTickets;
    private long totalCapacity;
    private double occupancyRate;
    private long checkedInTickets;
    private double checkInRate;
    private BigDecimal averageOrderValue;
    private List<HourlySalesItem> hourlySalesVelocity;
    private List<TierSalesItem> tierBreakdown;
    private Map<String, Double> paymentMethods;
    private List<CheckInFlowItem> rushHourCheckIn;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlySalesItem {
        private String timeSlot;
        private BigDecimal revenue;
        private int ticketsSold;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TierSalesItem {
        private String tierName;
        private BigDecimal price;
        private int soldCount;
        private int totalCapacity;
        private BigDecimal revenue;
        private double fillPercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckInFlowItem {
        private String timeSlot;
        private int count;
    }
}
