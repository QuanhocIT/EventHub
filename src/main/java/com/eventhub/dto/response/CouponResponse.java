package com.eventhub.dto.response;

import com.eventhub.enums.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Coupon details response")
public class CouponResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "EARLYBIRD10")
    private String code;

    @Schema(example = "10% off for early bird booking")
    private String description;

    @Schema(example = "PERCENTAGE")
    private DiscountType discountType;

    @Schema(example = "10.00")
    private BigDecimal discountValue;

    @Schema(example = "100000.00")
    private BigDecimal maxDiscountAmount;

    @Schema(example = "200000.00")
    private BigDecimal minOrderAmount;

    @Schema(example = "500")
    private Integer usageLimit;

    @Schema(example = "42")
    private Integer usedCount;

    @Schema(example = "2026-10-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(example = "2026-12-31T23:59:59")
    private LocalDateTime endDate;

    @Schema(example = "true")
    private boolean active;

    @Schema(example = "1")
    private Long eventId;

    @Schema(example = "Vietnam Tech Summit 2026")
    private String eventTitle;

    @Schema(example = "2026-10-21T09:00:00")
    private LocalDateTime createdAt;
}
