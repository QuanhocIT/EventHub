package com.eventhub.services.impl;

import com.eventhub.dto.request.SeatHoldRequest;
import com.eventhub.dto.response.SeatHoldResponse;
import com.eventhub.entities.EventSeat;
import com.eventhub.enums.EventSeatStatus;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.EventSeatRepository;
import com.eventhub.services.SeatHoldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatHoldServiceImpl implements SeatHoldService {

    private final EventSeatRepository eventSeatRepository;
    private final EventRepository eventRepository;
    private final ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider;

    private static final long HOLD_DURATION_SECONDS = 300; // 5 minutes

    @Override
    @Transactional
    public SeatHoldResponse holdSeats(SeatHoldRequest request, String currentUsername) {
        log.info("Holding seats: {} for event ID: {} by user: {}", request.getEventSeatIds(), request.getEventId(), currentUsername);

        if (!eventRepository.existsById(request.getEventId())) {
            throw new ResourceNotFoundException("Event", "id", request.getEventId());
        }

        List<Long> heldSeatIds = new ArrayList<>();
        RedisTemplate<String, Object> redisTemplate = redisTemplateProvider.getIfAvailable();

        for (Long seatId : request.getEventSeatIds()) {
            EventSeat eventSeat = eventSeatRepository.findByIdWithLock(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("EventSeat", "id", seatId));

            if (!eventSeat.getEvent().getId().equals(request.getEventId())) {
                throw new BadRequestException("Seat ID " + seatId + " does not belong to event ID " + request.getEventId());
            }

            String redisKey = buildRedisKey(request.getEventId(), seatId);

            if (redisTemplate != null) {
                Object existingHolder = redisTemplate.opsForValue().get(redisKey);
                if (existingHolder != null && !existingHolder.toString().equals(currentUsername)) {
                    throw new BadRequestException(String.format("Seat %s%s is currently held by another user",
                            eventSeat.getSeat().getSeatRow(), eventSeat.getSeat().getSeatNumber()));
                }
            }

            if (eventSeat.getStatus() != EventSeatStatus.AVAILABLE) {
                // If it is already HELD by the same user, permit refreshing
                boolean heldBySameUser = false;
                if (redisTemplate != null) {
                    Object holder = redisTemplate.opsForValue().get(redisKey);
                    heldBySameUser = currentUsername.equals(holder);
                }

                if (!heldBySameUser) {
                    throw new BadRequestException(String.format("Seat %s%s is not available (Status: %s)",
                            eventSeat.getSeat().getSeatRow(), eventSeat.getSeat().getSeatNumber(), eventSeat.getStatus()));
                }
            }

            // Apply Hold
            eventSeat.setStatus(EventSeatStatus.HELD);
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(redisKey, currentUsername, HOLD_DURATION_SECONDS, TimeUnit.SECONDS);
            }

            heldSeatIds.add(seatId);
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(HOLD_DURATION_SECONDS);
        log.info("Successfully held {} seats for user: {} until {}", heldSeatIds.size(), currentUsername, expiresAt);

        return SeatHoldResponse.builder()
                .eventId(request.getEventId())
                .eventSeatIds(heldSeatIds)
                .heldByUsername(currentUsername)
                .expiresAt(expiresAt)
                .holdDurationSeconds(HOLD_DURATION_SECONDS)
                .build();
    }

    @Override
    @Transactional
    public void releaseSeats(Long eventId, List<Long> eventSeatIds, String currentUsername) {
        log.info("Releasing seats: {} for event ID: {} by user: {}", eventSeatIds, eventId, currentUsername);
        RedisTemplate<String, Object> redisTemplate = redisTemplateProvider.getIfAvailable();

        for (Long seatId : eventSeatIds) {
            EventSeat eventSeat = eventSeatRepository.findByIdWithLock(seatId).orElse(null);
            if (eventSeat != null && eventSeat.getEvent().getId().equals(eventId)) {
                String redisKey = buildRedisKey(eventId, seatId);
                if (redisTemplate != null) {
                    Object holder = redisTemplate.opsForValue().get(redisKey);
                    if (holder != null && !holder.toString().equals(currentUsername)) {
                        continue; // Do not release another user's hold
                    }
                    redisTemplate.delete(redisKey);
                }

                if (eventSeat.getStatus() == EventSeatStatus.HELD) {
                    eventSeat.setStatus(EventSeatStatus.AVAILABLE);
                }
            }
        }
    }

    @Override
    public boolean isSeatHeldByAnother(Long eventId, Long eventSeatId, String currentUsername) {
        RedisTemplate<String, Object> redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return false;
        }
        String redisKey = buildRedisKey(eventId, eventSeatId);
        Object holder = redisTemplate.opsForValue().get(redisKey);
        return holder != null && !holder.toString().equals(currentUsername);
    }

    private String buildRedisKey(Long eventId, Long eventSeatId) {
        return String.format("seat_hold:%d:%d", eventId, eventSeatId);
    }
}
