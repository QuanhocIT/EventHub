package com.eventhub.controllers;

import com.eventhub.common.ApiResponse;
import com.eventhub.dto.request.CreateCouponRequest;
import com.eventhub.dto.request.ValidateCouponRequest;
import com.eventhub.dto.response.CouponCalculationResponse;
import com.eventhub.dto.response.CouponResponse;
import com.eventhub.services.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupons", description = "APIs for discount coupons and promotions")
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "Validate and preview coupon discount", description = "Check if a coupon is valid for a given event and calculate discount")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<CouponCalculationResponse>> validateCoupon(
            @Valid @RequestBody ValidateCouponRequest request
    ) {
        CouponCalculationResponse response = couponService.validateAndCalculate(
                request.getCode(), request.getEventId(), request.getOrderAmount());
        return ResponseEntity.ok(ApiResponse.success("Coupon verified successfully", response));
    }

    @Operation(summary = "Get list of coupons", description = "Only ADMIN or ORGANIZER can view coupons")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CouponResponse>>> getAllCoupons(Pageable pageable) {
        Page<CouponResponse> coupons = couponService.getAllCoupons(pageable);
        return ResponseEntity.ok(ApiResponse.success("Coupons retrieved successfully", coupons));
    }

    @Operation(summary = "Get coupon by code", description = "Get coupon details by code")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<CouponResponse>> getCouponByCode(@PathVariable String code) {
        CouponResponse coupon = couponService.getCouponByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Coupon retrieved successfully", coupon));
    }

    @Operation(summary = "Create coupon", description = "ADMIN or ORGANIZER can create a new discount coupon")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CreateCouponRequest request,
            Authentication authentication
    ) {
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        CouponResponse coupon = couponService.createCoupon(request, currentUsername, isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created successfully", coupon));
    }

    @Operation(summary = "Delete coupon", description = "ADMIN or event owner ORGANIZER can delete coupon")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        couponService.deleteCoupon(id, currentUsername, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Coupon deleted successfully", null));
    }
}
