package com.eventhub.services.impl;

import com.eventhub.dto.request.CreateCouponRequest;
import com.eventhub.dto.response.CouponCalculationResponse;
import com.eventhub.dto.response.CouponResponse;
import com.eventhub.entities.Coupon;
import com.eventhub.entities.Event;
import com.eventhub.enums.DiscountType;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ForbiddenException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.CouponRepository;
import com.eventhub.repositories.EventRepository;
import com.eventhub.services.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final EventRepository eventRepository;

    @Override
    public CouponCalculationResponse validateAndCalculate(String code, Long eventId, BigDecimal orderAmount) {
        log.debug("Validating coupon: {} for eventId: {}, orderAmount: {}", code, eventId, orderAmount);

        if (code == null || code.trim().isEmpty()) {
            throw new BadRequestException("Coupon code cannot be empty");
        }

        Coupon coupon = couponRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", code.trim().toUpperCase()));

        validateCouponEligibility(coupon, eventId, orderAmount);

        BigDecimal discountAmount = calculateDiscount(coupon, orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        String msg = coupon.getDiscountType() == DiscountType.PERCENTAGE
                ? String.format("Coupon applied: %s%% off (saved %s)", coupon.getDiscountValue(), discountAmount)
                : String.format("Coupon applied: %s off", discountAmount);

        return CouponCalculationResponse.builder()
                .code(coupon.getCode())
                .valid(true)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .message(msg)
                .build();
    }

    @Override
    @Transactional
    public BigDecimal applyCouponAndIncrementUsage(String code, Long eventId, BigDecimal orderAmount) {
        log.info("Applying and locking coupon: {} for event: {}", code, eventId);

        Coupon coupon = couponRepository.findByCodeWithLock(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", code.trim().toUpperCase()));

        validateCouponEligibility(coupon, eventId, orderAmount);

        BigDecimal discount = calculateDiscount(coupon, orderAmount);
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        log.info("Coupon {} applied successfully. New used count: {}/{}", coupon.getCode(), coupon.getUsedCount(), coupon.getUsageLimit());
        return discount;
    }

    @Override
    @Transactional
    public void releaseCouponUsage(String code) {
        if (code == null || code.trim().isEmpty()) return;
        log.info("Releasing coupon usage for: {}", code);
        couponRepository.findByCodeWithLock(code.trim()).ifPresent(coupon -> {
            coupon.setUsedCount(Math.max(0, coupon.getUsedCount() - 1));
            couponRepository.save(coupon);
            log.info("Coupon {} usage released. New used count: {}", coupon.getCode(), coupon.getUsedCount());
        });
    }

    @Override
    @Transactional
    public CouponResponse createCoupon(CreateCouponRequest request, String currentUsername, boolean isAdmin) {
        String upperCode = request.getCode().trim().toUpperCase();
        log.info("Creating new coupon: {} by user: {} (isAdmin: {})", upperCode, currentUsername, isAdmin);

        if (couponRepository.existsByCodeIgnoreCase(upperCode)) {
            throw new BadRequestException("Coupon with code '" + upperCode + "' already exists");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Coupon end date cannot be before start date");
        }

        Event event = null;
        if (request.getEventId() != null) {
            event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new ResourceNotFoundException("Event", "id", request.getEventId()));

            boolean isOwner = event.getOrganizer() != null && event.getOrganizer().getUsername().equals(currentUsername);
            if (!isAdmin && !isOwner) {
                throw new ForbiddenException("You do not have permission to create coupon for this event");
            }
        }

        Coupon coupon = Coupon.builder()
                .code(upperCode)
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO)
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(true)
                .event(event)
                .build();

        Coupon saved = couponRepository.save(coupon);
        log.info("Coupon created successfully with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    public Page<CouponResponse> getAllCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public CouponResponse getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", code.trim().toUpperCase()));
        return mapToResponse(coupon);
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id, String currentUsername, boolean isAdmin) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));

        if (!isAdmin) {
            if (coupon.getEvent() == null || coupon.getEvent().getOrganizer() == null
                    || !coupon.getEvent().getOrganizer().getUsername().equals(currentUsername)) {
                throw new ForbiddenException("You do not have permission to delete this coupon");
            }
        }

        couponRepository.delete(coupon);
        log.info("Coupon ID: {} deleted by user: {}", id, currentUsername);
    }

    private void validateCouponEligibility(Coupon coupon, Long eventId, BigDecimal orderAmount) {
        if (!coupon.isActive()) {
            throw new BadRequestException("Coupon is currently deactivated");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartDate())) {
            throw new BadRequestException("Coupon promotion has not started yet");
        }
        if (now.isAfter(coupon.getEndDate())) {
            throw new BadRequestException("Coupon has expired");
        }

        if (coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new BadRequestException("Coupon usage limit has been reached");
        }

        if (coupon.getEvent() != null && !coupon.getEvent().getId().equals(eventId)) {
            throw new BadRequestException("Coupon is only applicable for event: " + coupon.getEvent().getTitle());
        }

        if (orderAmount != null && coupon.getMinOrderAmount() != null
                && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BadRequestException(String.format("Minimum order amount of %s is required to apply this coupon",
                    coupon.getMinOrderAmount()));
        }
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = orderAmount.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                discount = coupon.getMaxDiscountAmount();
            }
        } else {
            discount = coupon.getDiscountValue();
        }

        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }
        return discount;
    }

    private CouponResponse mapToResponse(Coupon coupon) {
        Long eventId = coupon.getEvent() != null ? coupon.getEvent().getId() : null;
        String eventTitle = coupon.getEvent() != null ? coupon.getEvent().getTitle() : null;

        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .minOrderAmount(coupon.getMinOrderAmount())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .active(coupon.isActive())
                .eventId(eventId)
                .eventTitle(eventTitle)
                .createdAt(coupon.getCreatedAt())
                .build();
    }
}
