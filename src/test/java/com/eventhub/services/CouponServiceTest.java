package com.eventhub.services;

import com.eventhub.dto.request.CreateCouponRequest;
import com.eventhub.dto.response.CouponCalculationResponse;
import com.eventhub.dto.response.CouponResponse;
import com.eventhub.entities.Coupon;
import com.eventhub.enums.DiscountType;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.repositories.CouponRepository;
import com.eventhub.repositories.EventRepository;
import com.eventhub.services.impl.CouponServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Coupon percentageCoupon;
    private Coupon fixedCoupon;

    @BeforeEach
    void setUp() {
        percentageCoupon = Coupon.builder()
                .id(1L)
                .code("PROMO10")
                .description("10% discount up to 50k")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10.0))
                .maxDiscountAmount(BigDecimal.valueOf(50000))
                .minOrderAmount(BigDecimal.valueOf(100000))
                .usageLimit(100)
                .usedCount(10)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();

        fixedCoupon = Coupon.builder()
                .id(2L)
                .code("SAVE50K")
                .description("50k flat discount")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(50000))
                .minOrderAmount(BigDecimal.valueOf(200000))
                .usageLimit(50)
                .usedCount(5)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should calculate percentage discount correctly with cap")
    void validateAndCalculate_PercentageDiscount_WithCap() {
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(percentageCoupon));

        // 10% of 1,000,000 = 100,000, but capped at 50,000
        CouponCalculationResponse response = couponService.validateAndCalculate("PROMO10", 1L, BigDecimal.valueOf(1000000));

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(response.getFinalAmount()).isEqualByComparingTo(BigDecimal.valueOf(950000));
    }

    @Test
    @DisplayName("Should calculate fixed amount discount correctly")
    void validateAndCalculate_FixedAmount_Success() {
        when(couponRepository.findByCodeIgnoreCase("SAVE50K")).thenReturn(Optional.of(fixedCoupon));

        CouponCalculationResponse response = couponService.validateAndCalculate("SAVE50K", 1L, BigDecimal.valueOf(300000));

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(response.getFinalAmount()).isEqualByComparingTo(BigDecimal.valueOf(250000));
    }

    @Test
    @DisplayName("Should throw BadRequestException when order amount is below minOrderAmount")
    void validateAndCalculate_BelowMinOrder_ThrowsException() {
        when(couponRepository.findByCodeIgnoreCase("SAVE50K")).thenReturn(Optional.of(fixedCoupon));

        // Order amount 150,000 < min 200,000
        assertThatThrownBy(() -> couponService.validateAndCalculate("SAVE50K", 1L, BigDecimal.valueOf(150000)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Minimum order amount");
    }

    @Test
    @DisplayName("Should throw BadRequestException when coupon is expired")
    void validateAndCalculate_Expired_ThrowsException() {
        percentageCoupon.setEndDate(LocalDateTime.now().minusDays(1));
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(percentageCoupon));

        assertThatThrownBy(() -> couponService.validateAndCalculate("PROMO10", 1L, BigDecimal.valueOf(500000)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("Should throw BadRequestException when usage limit is reached")
    void validateAndCalculate_UsageLimitReached_ThrowsException() {
        percentageCoupon.setUsedCount(100);
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(percentageCoupon));

        assertThatThrownBy(() -> couponService.validateAndCalculate("PROMO10", 1L, BigDecimal.valueOf(500000)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("usage limit has been reached");
    }

    @Test
    @DisplayName("Should apply coupon and atomically increment used count")
    void applyCouponAndIncrementUsage_Success() {
        when(couponRepository.findByCodeWithLock("PROMO10")).thenReturn(Optional.of(percentageCoupon));

        BigDecimal discount = couponService.applyCouponAndIncrementUsage("PROMO10", 1L, BigDecimal.valueOf(200000));

        // 10% of 200,000 = 20,000
        assertThat(discount).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(percentageCoupon.getUsedCount()).isEqualTo(11);
        verify(couponRepository).save(percentageCoupon);
    }

    @Test
    @DisplayName("Should release coupon and decrement used count")
    void releaseCouponUsage_Success() {
        when(couponRepository.findByCodeWithLock("PROMO10")).thenReturn(Optional.of(percentageCoupon));

        couponService.releaseCouponUsage("PROMO10");

        assertThat(percentageCoupon.getUsedCount()).isEqualTo(9);
        verify(couponRepository).save(percentageCoupon);
    }

    @Test
    @DisplayName("Should create coupon successfully")
    void createCoupon_Success() {
        CreateCouponRequest request = CreateCouponRequest.builder()
                .code("NEWYEAR2026")
                .description("New year promo")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(15.0))
                .minOrderAmount(BigDecimal.valueOf(100000))
                .usageLimit(200)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(60))
                .build();

        when(couponRepository.existsByCodeIgnoreCase("NEWYEAR2026")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(i -> {
            Coupon c = i.getArgument(0);
            c.setId(10L);
            return c;
        });

        CouponResponse response = couponService.createCoupon(request, "admin", true);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("NEWYEAR2026");
        verify(couponRepository).save(any(Coupon.class));
    }
}
