package com.eventhub.services;

import com.eventhub.dto.request.SeatHoldRequest;
import com.eventhub.dto.response.SeatHoldResponse;

import java.util.List;

public interface SeatHoldService {

    SeatHoldResponse holdSeats(SeatHoldRequest request, String currentUsername);

    void releaseSeats(Long eventId, List<Long> eventSeatIds, String currentUsername);

    boolean isSeatHeldByAnother(Long eventId, Long eventSeatId, String currentUsername);
}
