package com.eventhub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Coupon discount calculation result")
public class CouponCalculationResponse {

    @Schema(example = "EARLYBIRD10")
    private String code;

    @Schema(example = "true")
    private boolean valid;

    @Schema(example = "50000.00")
    private BigDecimal discountAmount;

    @Schema(example = "450000.00")
    private BigDecimal finalAmount;

    @Schema(example = "Coupon applied successfully (10% discount)")
    private String message;
}
