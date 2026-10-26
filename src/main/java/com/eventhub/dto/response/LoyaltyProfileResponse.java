package com.eventhub.dto.response;

import com.eventhub.enums.LoyaltyTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyProfileResponse {
    private String username;
    private String fullName;
    private Integer pointsBalance;
    private Integer lifetimePoints;
    private LoyaltyTier tier;
    private String tierDisplayName;
    private int discountPercent;
    private int nextTierPointsRequired;
    private double nextTierProgressPercent;
    private List<LoyaltyTransactionResponse> recentTransactions;
}
