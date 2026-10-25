package com.eventhub.services.impl;

import com.eventhub.dto.request.SeatHoldRequest;
import com.eventhub.dto.response.SeatHoldResponse;
import com.eventhub.entities.EventSeat;
import com.eventhub.enums.EventSeatStatus;
import com.eventhub.events.SeatStatusChangedEvent;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.EventSeatRepository;
import com.eventhub.services.SeatHoldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final long HOLD_DURATION_SECONDS = 900; // 15 minutes TTL

    @Override
    @Transactional
    public SeatHoldResponse holdSeats(SeatHoldRequest request, String currentUsername) {
        log.info("Holding seats: {} for event ID: {} by user: {}", request.getEventSeatIds(), request.getEventId(), currentUsername);

        if (!eventRepository.existsById(request.getEventId())) {
            throw new ResourceNotFoundException("Event", "id", request.getEventId());
        }

        List<Long> heldSeatIds = new ArrayList<>();
        RedisTemplate<String, Object> redisTemplate = redisTemplateProvider.getIfAvailable();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(HOLD_DURATION_SECONDS);

        for (Long seatId : request.getEventSeatIds()) {
            EventSeat eventSeat = eventSeatRepository.findByIdWithLock(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("EventSeat", "id", seatId));

            if (!eventSeat.getEvent().getId().equals(request.getEventId())) {
                throw new BadRequestException("Seat ID " + seatId + " does not belong to event ID " + request.getEventId());
            }

            String redisKey = buildRedisKey(request.getEventId(), seatId);

            // Auto-reclaim if hold has expired in DB
            if (eventSeat.getStatus() == EventSeatStatus.HELD && eventSeat.getHeldExpiresAt() != null && eventSeat.getHeldExpiresAt().isBefore(now)) {
                log.info("Seat {} hold expired at {}. Reclaiming as AVAILABLE for new hold.", seatId, eventSeat.getHeldExpiresAt());
                eventSeat.setStatus(EventSeatStatus.AVAILABLE);
                eventSeat.setHeldByUsername(null);
                eventSeat.setHeldExpiresAt(null);
                if (redisTemplate != null) {
                    redisTemplate.delete(redisKey);
                }
            }

            // Check if held by another in Redis
            if (redisTemplate != null) {
                Object existingHolder = redisTemplate.opsForValue().get(redisKey);
                if (existingHolder != null && !existingHolder.toString().equals(currentUsername)) {
                    throw new BadRequestException(String.format("Ghế %s%s hiện đang được giữ chỗ bởi khách hàng khác!",
                            eventSeat.getSeat().getSeatRow(), eventSeat.getSeat().getSeatNumber()));
                }
            }

            // Check if held by another in Database
            if (eventSeat.getStatus() != EventSeatStatus.AVAILABLE) {
                boolean heldBySameUser = currentUsername.equals(eventSeat.getHeldByUsername());
                if (!heldBySameUser && redisTemplate != null) {
                    Object holder = redisTemplate.opsForValue().get(redisKey);
                    heldBySameUser = currentUsername.equals(holder);
                }

                if (!heldBySameUser) {
                    throw new BadRequestException(String.format("Ghế %s%s không còn khả dụng (Trạng thái: %s)",
                            eventSeat.getSeat().getSeatRow(), eventSeat.getSeat().getSeatNumber(), eventSeat.getStatus()));
                }
            }

            // Apply Hold in DB and Redis
            eventSeat.setStatus(EventSeatStatus.HELD);
            eventSeat.setHeldByUsername(currentUsername);
            eventSeat.setHeldExpiresAt(expiresAt);

            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(redisKey, currentUsername, HOLD_DURATION_SECONDS, TimeUnit.SECONDS);
            }

            heldSeatIds.add(seatId);
        }

        // Publish event for real-time notification
        applicationEventPublisher.publishEvent(SeatStatusChangedEvent.builder()
                .eventId(request.getEventId())
                .seatIds(heldSeatIds)
                .action("HELD")
                .heldByUsername(currentUsername)
                .build());

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
        List<Long> releasedSeatIds = new ArrayList<>();

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
                    // Check if current user is owner or admin
                    if (currentUsername == null || currentUsername.equals(eventSeat.getHeldByUsername())) {
                        eventSeat.setStatus(EventSeatStatus.AVAILABLE);
                        eventSeat.setHeldByUsername(null);
                        eventSeat.setHeldExpiresAt(null);
                        releasedSeatIds.add(seatId);
                    }
                }
            }
        }

        if (!releasedSeatIds.isEmpty()) {
            applicationEventPublisher.publishEvent(SeatStatusChangedEvent.builder()
                    .eventId(eventId)
                    .seatIds(releasedSeatIds)
                    .action("RELEASED")
                    .heldByUsername(currentUsername)
                    .build());
        }
    }

    @Override
    public boolean isSeatHeldByAnother(Long eventId, Long eventSeatId, String currentUsername) {
        RedisTemplate<String, Object> redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            String redisKey = buildRedisKey(eventId, eventSeatId);
            Object holder = redisTemplate.opsForValue().get(redisKey);
            if (holder != null) {
                return !holder.toString().equals(currentUsername);
            }
        }

        return eventSeatRepository.findById(eventSeatId)
                .map(es -> es.getStatus() == EventSeatStatus.HELD &&
                        es.getHeldByUsername() != null &&
                        !es.getHeldByUsername().equals(currentUsername) &&
                        (es.getHeldExpiresAt() == null || es.getHeldExpiresAt().isAfter(LocalDateTime.now())))
                .orElse(false);
    }

    @Override
    public boolean isSeatHeldByCurrentUser(Long eventId, Long eventSeatId, String currentUsername) {
        if (currentUsername == null) return false;
        RedisTemplate<String, Object> redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            String redisKey = buildRedisKey(eventId, eventSeatId);
            Object holder = redisTemplate.opsForValue().get(redisKey);
            if (holder != null) {
                return currentUsername.equals(holder.toString());
            }
        }

        return eventSeatRepository.findById(eventSeatId)
                .map(es -> es.getStatus() == EventSeatStatus.HELD &&
                        currentUsername.equals(es.getHeldByUsername()) &&
                        (es.getHeldExpiresAt() == null || es.getHeldExpiresAt().isAfter(LocalDateTime.now())))
                .orElse(false);
    }

    private String buildRedisKey(Long eventId, Long eventSeatId) {
        return String.format("seat_hold:%d:%d", eventId, eventSeatId);
    }
}
