package com.eventhub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
@Schema(description = "Request payload to configure custom visual seat map and pricing for an event")
public class ConfigureEventSeatsRequest {

    @Schema(description = "Optional venue ID to associate with event")
    private Long venueId;

    @Schema(description = "Default price applied if not specified per item", example = "300000")
    private BigDecimal defaultPrice;

    @NotEmpty(message = "Seat layouts cannot be empty")
    @Valid
    private List<EventSeatConfigItemRequest> seats;
}
