package com.eventhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateStatsResponse {
    private Long eventId;
    private String eventTitle;
    private LocalDateTime eventStartTime;
    private String location;
    private long totalTickets;
    private long checkedInCount;
    private long remainingCount;
    private double checkInRatePercent;
    private List<GateTierStatResponse> tierStats;
    private List<CheckInResponse> recentCheckIns;
}
