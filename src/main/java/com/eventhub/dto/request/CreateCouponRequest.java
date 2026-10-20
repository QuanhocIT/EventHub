package com.eventhub.dto.request;

import com.eventhub.enums.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a new Coupon")
public class CreateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 30, message = "Coupon code must be between 3 and 30 characters")
    @Schema(example = "EARLYBIRD10", description = "Coupon promo code (case-insensitive)")
    private String code;

    @Schema(example = "10% off for early bird booking", description = "Description of coupon")
    private String description;

    @NotNull(message = "Discount type is required (PERCENTAGE or FIXED_AMOUNT)")
    @Schema(example = "PERCENTAGE")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    @Schema(example = "10.0", description = "10 for 10% or 50000 for 50.000 VND")
    private BigDecimal discountValue;

    @Schema(example = "100000", description = "Maximum discount amount in currency for PERCENTAGE type")
    private BigDecimal maxDiscountAmount;

    @DecimalMin(value = "0.0", message = "Minimum order amount cannot be negative")
    @Schema(example = "200000", description = "Minimum subtotal required to apply this coupon")
    private BigDecimal minOrderAmount;

    @NotNull(message = "Usage limit is required")
    @Min(value = 1, message = "Usage limit must be at least 1")
    @Schema(example = "500", description = "Maximum number of times this coupon can be used")
    private Integer usageLimit;

    @NotNull(message = "Start date is required")
    @Schema(example = "2026-10-01T00:00:00")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Schema(example = "2026-12-31T23:59:59")
    private LocalDateTime endDate;

    @Schema(example = "1", description = "Optional event ID if coupon applies only to a specific event")
    private Long eventId;
}
