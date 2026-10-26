package com.eventhub.services;

import com.eventhub.dto.response.LoyaltyProfileResponse;
import com.eventhub.entities.User;
import com.eventhub.enums.LoyaltyActivityType;

public interface LoyaltyService {
    LoyaltyProfileResponse getLoyaltyProfile(String username);
    void awardPoints(User user, int points, LoyaltyActivityType type, String description);
    void awardPointsByUsername(String username, int points, LoyaltyActivityType type, String description);
}
