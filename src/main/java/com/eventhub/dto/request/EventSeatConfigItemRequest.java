package com.eventhub.dto.request;

import com.eventhub.enums.EventSeatStatus;
import com.eventhub.enums.SeatZone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
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
public class EventSeatConfigItemRequest {

    @NotBlank(message = "Seat row is required")
    @Schema(example = "A")
    private String seatRow;

    @NotNull(message = "Seat number is required")
    @Min(value = 1, message = "Seat number must be >= 1")
    @Schema(example = "1")
    private Integer seatNumber;

    @NotNull(message = "Zone is required")
    @Schema(example = "VIP")
    private SeatZone zone;

    @NotNull(message = "Seat price is required")
    @Min(value = 0, message = "Price must be >= 0")
    @Schema(example = "500000")
    private BigDecimal price;

    @NotNull(message = "Status is required")
    @Schema(example = "AVAILABLE")
    private EventSeatStatus status;
}
