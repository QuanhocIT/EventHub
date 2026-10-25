package com.eventhub.dto.request;

import com.eventhub.enums.SeatZone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalSeatItemRequest {

    @NotBlank(message = "Row label is required (e.g. A, B, C)")
    @Schema(example = "A")
    private String seatRow;

    @NotNull(message = "Seat number is required")
    @Min(value = 1, message = "Seat number must be at least 1")
    @Schema(example = "1")
    private Integer seatNumber;

    @NotNull(message = "Seat zone is required")
    @Schema(example = "VIP")
    private SeatZone zone;
}
