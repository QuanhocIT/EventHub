package com.eventhub.dto.response;

import com.eventhub.enums.EventSeatStatus;
import com.eventhub.enums.SeatZone;
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
@Schema(description = "Event seat with status and pricing")
public class EventSeatResponse {

    @Schema(description = "EventSeat ID", example = "101")
    private Long id;

    @Schema(description = "Physical Seat ID", example = "1")
    private Long seatId;

    @Schema(description = "Row identifier", example = "A")
    private String seatRow;

    @Schema(description = "Seat number in row", example = "5")
    private Integer seatNumber;

    @Schema(description = "Seat zone category", example = "VIP")
    private SeatZone zone;

    @Schema(description = "Availability status for this specific event", example = "AVAILABLE")
    private EventSeatStatus status;

    @Schema(description = "Seat price for this event", example = "150.00")
    private BigDecimal price;

    @Schema(description = "Whether this seat is held by the requesting user", example = "true")
    private Boolean heldByCurrentUser;

    @Schema(description = "Expiration timestamp of the seat hold", example = "2026-10-25T15:30:00")
    private java.time.LocalDateTime heldExpiresAt;

    @Schema(description = "Remaining seconds of the hold", example = "854")
    private Long remainingHoldSeconds;
}
