package com.eventhub.dto.response;

import com.eventhub.enums.LoyaltyActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTransactionResponse {
    private Long id;
    private Integer pointsChange;
    private LoyaltyActivityType activityType;
    private String activityName;
    private String description;
    private LocalDateTime createdAt;
}
