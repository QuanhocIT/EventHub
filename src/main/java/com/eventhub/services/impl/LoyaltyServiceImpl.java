package com.eventhub.services.impl;

import com.eventhub.dto.response.LoyaltyProfileResponse;
import com.eventhub.dto.response.LoyaltyTransactionResponse;
import com.eventhub.entities.LoyaltyTransaction;
import com.eventhub.entities.User;
import com.eventhub.entities.UserLoyalty;
import com.eventhub.enums.LoyaltyActivityType;
import com.eventhub.enums.LoyaltyTier;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.LoyaltyTransactionRepository;
import com.eventhub.repositories.UserLoyaltyRepository;
import com.eventhub.repositories.UserRepository;
import com.eventhub.services.LoyaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyServiceImpl implements LoyaltyService {

    private final UserLoyaltyRepository userLoyaltyRepository;
    private final LoyaltyTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LoyaltyProfileResponse getLoyaltyProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        UserLoyalty loyalty = getOrCreateLoyalty(user);
        List<LoyaltyTransaction> transactions = transactionRepository.findTop20ByUserIdOrderByCreatedAtDesc(user.getId());

        List<LoyaltyTransactionResponse> txResponses = transactions.stream()
                .map(tx -> LoyaltyTransactionResponse.builder()
                        .id(tx.getId())
                        .pointsChange(tx.getPointsChange())
                        .activityType(tx.getActivityType())
                        .activityName(tx.getActivityType() != null ? tx.getActivityType().getDescription() : "")
                        .description(tx.getDescription())
                        .createdAt(tx.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // Calculate progress to next tier
        int currentPoints = loyalty.getLifetimePoints();
        int nextPoints = 500;
        if (loyalty.getTier() == LoyaltyTier.MEMBER) {
            nextPoints = 500;
        } else if (loyalty.getTier() == LoyaltyTier.SILVER) {
            nextPoints = 1500;
        } else if (loyalty.getTier() == LoyaltyTier.GOLD) {
            nextPoints = 3000;
        } else {
            nextPoints = 3000;
        }

        int pointsNeeded = Math.max(0, nextPoints - currentPoints);
        double progress = loyalty.getTier() == LoyaltyTier.DIAMOND ? 100.0 :
                Math.min(100.0, Math.round(((double) currentPoints / nextPoints) * 1000.0) / 10.0);

        return LoyaltyProfileResponse.builder()
                .username(user.getUsername())
                .fullName(user.getFullName() != null ? user.getFullName() : user.getUsername())
                .pointsBalance(loyalty.getPointsBalance())
                .lifetimePoints(loyalty.getLifetimePoints())
                .tier(loyalty.getTier())
                .tierDisplayName(loyalty.getTier().getDisplayName())
                .discountPercent(loyalty.getTier().getDiscountPercent())
                .nextTierPointsRequired(pointsNeeded)
                .nextTierProgressPercent(progress)
                .recentTransactions(txResponses)
                .build();
    }

    @Override
    @Transactional
    public void awardPointsByUsername(String username, int points, LoyaltyActivityType type, String description) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            awardPoints(user, points, type, description);
        }
    }

    @Override
    @Transactional
    public void awardPoints(User user, int points, LoyaltyActivityType type, String description) {
        if (user == null || points == 0) return;

        UserLoyalty loyalty = getOrCreateLoyalty(user);
        loyalty.setPointsBalance(loyalty.getPointsBalance() + points);
        if (points > 0) {
            loyalty.setLifetimePoints(loyalty.getLifetimePoints() + points);
        }

        // Auto Tier Upgrade Check
        int lt = loyalty.getLifetimePoints();
        if (lt >= LoyaltyTier.DIAMOND.getMinPoints()) {
            loyalty.setTier(LoyaltyTier.DIAMOND);
        } else if (lt >= LoyaltyTier.GOLD.getMinPoints()) {
            loyalty.setTier(LoyaltyTier.GOLD);
        } else if (lt >= LoyaltyTier.SILVER.getMinPoints()) {
            loyalty.setTier(LoyaltyTier.SILVER);
        } else {
            loyalty.setTier(LoyaltyTier.MEMBER);
        }

        userLoyaltyRepository.save(loyalty);

        LoyaltyTransaction tx = LoyaltyTransaction.builder()
                .user(user)
                .pointsChange(points)
                .activityType(type)
                .description(description)
                .build();
        transactionRepository.save(tx);

        log.info("Awarded {} loyalty points to user {} for activity: {}", points, user.getUsername(), type);
    }

    private UserLoyalty getOrCreateLoyalty(User user) {
        return userLoyaltyRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserLoyalty newLoyalty = UserLoyalty.builder()
                            .user(user)
                            .pointsBalance(100)
                            .lifetimePoints(100)
                            .tier(LoyaltyTier.MEMBER)
                            .build();
                    UserLoyalty saved = userLoyaltyRepository.save(newLoyalty);

                    LoyaltyTransaction welcomeTx = LoyaltyTransaction.builder()
                            .user(user)
                            .pointsChange(100)
                            .activityType(LoyaltyActivityType.SIGNUP_BONUS)
                            .description("Quà tặng 100 điểm thưởng thành viên mới EventHub")
                            .build();
                    transactionRepository.save(welcomeTx);

                    return saved;
                });
    }
}
