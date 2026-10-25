package com.eventhub.dto.request;

import com.eventhub.enums.EventSeatStatus;
import com.eventhub.enums.SeatZone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to batch update price, zone or status of event seats")
public class BatchUpdateEventSeatsRequest {

    @NotEmpty(message = "Event seat IDs cannot be empty")
    @Schema(description = "List of event seat IDs to update", example = "[101, 102, 103]")
    private List<Long> eventSeatIds;

    @Schema(description = "New price to apply (optional)", example = "450000")
    private BigDecimal price;

    @Schema(description = "New status to apply (optional, e.g. AVAILABLE, BLOCKED)", example = "BLOCKED")
    private EventSeatStatus status;

    @Schema(description = "New zone to apply (optional, e.g. VIP, STANDARD, BALCONY)", example = "VIP")
    private SeatZone zone;
}
