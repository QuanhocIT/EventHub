package com.eventhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHoldResponse {

    private Long eventId;
    private List<Long> eventSeatIds;
    private String heldByUsername;
    private LocalDateTime expiresAt;
    private Long holdDurationSeconds;
}
