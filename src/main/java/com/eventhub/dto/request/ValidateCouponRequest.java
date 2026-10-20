package com.eventhub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to validate and calculate coupon discount")
public class ValidateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Schema(example = "EARLYBIRD10")
    private String code;

    @NotNull(message = "Event ID is required")
    @Schema(example = "1")
    private Long eventId;

    @NotNull(message = "Order amount is required")
    @DecimalMin(value = "0.0", message = "Order amount cannot be negative")
    @Schema(example = "500000.00")
    private BigDecimal orderAmount;
}
