package com.eventhub.controllers;

import com.eventhub.common.ApiResponse;
import com.eventhub.dto.response.LoyaltyProfileResponse;
import com.eventhub.services.LoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loyalty")
@RequiredArgsConstructor
@Tag(name = "Loyalty & Rewards", description = "CineClub & EventHub Loyalty: Tích điểm, Thăng hạng thành viên & Đặc quyền VIP")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @Operation(summary = "Lấy hồ sơ điểm thưởng, hạng thành viên và lịch sử giao dịch điểm")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<LoyaltyProfileResponse>> getMyLoyaltyProfile(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "customer";
        LoyaltyProfileResponse response = loyaltyService.getLoyaltyProfile(username);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin điểm thưởng thành công", response));
    }
}
