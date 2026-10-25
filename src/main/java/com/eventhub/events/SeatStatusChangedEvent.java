package com.eventhub.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class SeatStatusChangedEvent {

    private final Long eventId;
    private final List<Long> seatIds;
    private final String action; // HELD, RELEASED, SOLD
    private final String heldByUsername;

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
}
