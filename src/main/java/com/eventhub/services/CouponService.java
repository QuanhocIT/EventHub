package com.eventhub.services;

import com.eventhub.dto.request.CreateCouponRequest;
import com.eventhub.dto.response.CouponCalculationResponse;
import com.eventhub.dto.response.CouponResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CouponService {

    CouponCalculationResponse validateAndCalculate(String code, Long eventId, BigDecimal orderAmount);

    CouponResponse createCoupon(CreateCouponRequest request, String currentUsername, boolean isAdmin);

    Page<CouponResponse> getAllCoupons(Pageable pageable);

    CouponResponse getCouponByCode(String code);

    void deleteCoupon(Long id, String currentUsername, boolean isAdmin);

    BigDecimal applyCouponAndIncrementUsage(String code, Long eventId, BigDecimal orderAmount);

    void releaseCouponUsage(String code);
}
