package com.eventhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateTierStatResponse {
    private Long ticketTypeId;
    private String ticketTypeName;
    private long totalCount;
    private long checkedInCount;
    private long remainingCount;
    private double checkInRatePercent;
}
