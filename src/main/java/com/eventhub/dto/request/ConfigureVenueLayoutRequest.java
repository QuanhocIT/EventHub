package com.eventhub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to configure physical seating layout for a venue")
public class ConfigureVenueLayoutRequest {

    @Schema(description = "List of physical seats defining the layout", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Seats cannot be empty")
    @Valid
    private List<PhysicalSeatItemRequest> seats;
}
