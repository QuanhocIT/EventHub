package com.eventhub.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class BookingConfirmedEvent {

    private final Long bookingId;
    private final String bookingCode;
    private final String attendeeEmail;
    private final String attendeeName;
    private final String eventTitle;
    private final BigDecimal totalAmount;

    @Builder.Default
    private final LocalDateTime eventTime = LocalDateTime.now();
}
