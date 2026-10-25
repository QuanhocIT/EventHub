package com.eventhub.services;

import com.eventhub.dto.request.SeatHoldRequest;
import com.eventhub.dto.response.SeatHoldResponse;
import com.eventhub.entities.Event;
import com.eventhub.entities.EventSeat;
import com.eventhub.entities.Seat;
import com.eventhub.enums.EventSeatStatus;
import com.eventhub.enums.SeatZone;
import com.eventhub.events.SeatStatusChangedEvent;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.EventSeatRepository;
import com.eventhub.services.impl.SeatHoldServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatHoldServiceTest {

    @Mock
    private EventSeatRepository eventSeatRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private SeatHoldServiceImpl seatHoldService;

    private Event event;
    private EventSeat availableSeat;

    @BeforeEach
    void setUp() {
        event = Event.builder().id(10L).title("Concert").build();

        Seat seat = Seat.builder()
                .seatRow("A")
                .seatNumber(1)
                .zone(SeatZone.VIP)
                .build();

        availableSeat = EventSeat.builder()
                .id(100L)
                .event(event)
                .seat(seat)
                .status(EventSeatStatus.AVAILABLE)
                .build();
    }

    @Test
    @DisplayName("Should hold seat successfully when available and set Redis key & DB hold")
    void holdSeats_Success() {
        SeatHoldRequest request = SeatHoldRequest.builder()
                .eventId(10L)
                .eventSeatIds(List.of(100L))
                .build();

        when(eventRepository.existsById(10L)).thenReturn(true);
        when(redisTemplateProvider.getIfAvailable()).thenReturn(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("seat_hold:10:100")).thenReturn(null);
        when(eventSeatRepository.findByIdWithLock(100L)).thenReturn(Optional.of(availableSeat));

        SeatHoldResponse response = seatHoldService.holdSeats(request, "alice");

        assertThat(response).isNotNull();
        assertThat(response.getEventSeatIds()).containsExactly(100L);
        assertThat(response.getHeldByUsername()).isEqualTo("alice");
        assertThat(availableSeat.getStatus()).isEqualTo(EventSeatStatus.HELD);
        assertThat(availableSeat.getHeldByUsername()).isEqualTo("alice");
        assertThat(availableSeat.getHeldExpiresAt()).isAfter(LocalDateTime.now());

        verify(valueOperations).set(eq("seat_hold:10:100"), eq("alice"), anyLong(), eq(TimeUnit.SECONDS));
        verify(applicationEventPublisher).publishEvent(any(SeatStatusChangedEvent.class));
    }

    @Test
    @DisplayName("Should auto-reclaim seat if hold has expired in DB and allow new hold")
    void holdSeats_AutoReclaimExpiredHold() {
        availableSeat.setStatus(EventSeatStatus.HELD);
        availableSeat.setHeldByUsername("charlie");
        availableSeat.setHeldExpiresAt(LocalDateTime.now().minusMinutes(5)); // expired

        SeatHoldRequest request = SeatHoldRequest.builder()
                .eventId(10L)
                .eventSeatIds(List.of(100L))
                .build();

        when(eventRepository.existsById(10L)).thenReturn(true);
        when(redisTemplateProvider.getIfAvailable()).thenReturn(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("seat_hold:10:100")).thenReturn(null);
        when(eventSeatRepository.findByIdWithLock(100L)).thenReturn(Optional.of(availableSeat));

        SeatHoldResponse response = seatHoldService.holdSeats(request, "alice");

        assertThat(response).isNotNull();
        assertThat(response.getHeldByUsername()).isEqualTo("alice");
        assertThat(availableSeat.getStatus()).isEqualTo(EventSeatStatus.HELD);
        assertThat(availableSeat.getHeldByUsername()).isEqualTo("alice");
    }

    @Test
    @DisplayName("Should throw BadRequestException when seat is already held by another user in Redis")
    void holdSeats_Conflict_WhenHeldByAnother() {
        SeatHoldRequest request = SeatHoldRequest.builder()
                .eventId(10L)
                .eventSeatIds(List.of(100L))
                .build();

        when(eventRepository.existsById(10L)).thenReturn(true);
        when(redisTemplateProvider.getIfAvailable()).thenReturn(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("seat_hold:10:100")).thenReturn("bob");
        when(eventSeatRepository.findByIdWithLock(100L)).thenReturn(Optional.of(availableSeat));

        assertThatThrownBy(() -> seatHoldService.holdSeats(request, "alice"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("hiện đang được giữ chỗ");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when event not found")
    void holdSeats_EventNotFound() {
        SeatHoldRequest request = SeatHoldRequest.builder()
                .eventId(999L)
                .eventSeatIds(List.of(100L))
                .build();

        when(eventRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> seatHoldService.holdSeats(request, "alice"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should release seat, reset fields, and remove Redis key")
    void releaseSeats_Success() {
        availableSeat.setStatus(EventSeatStatus.HELD);
        availableSeat.setHeldByUsername("alice");

        when(redisTemplateProvider.getIfAvailable()).thenReturn(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("seat_hold:10:100")).thenReturn("alice");
        when(eventSeatRepository.findByIdWithLock(100L)).thenReturn(Optional.of(availableSeat));

        seatHoldService.releaseSeats(10L, List.of(100L), "alice");

        assertThat(availableSeat.getStatus()).isEqualTo(EventSeatStatus.AVAILABLE);
        assertThat(availableSeat.getHeldByUsername()).isNull();
        assertThat(availableSeat.getHeldExpiresAt()).isNull();
        verify(redisTemplate).delete("seat_hold:10:100");
        verify(applicationEventPublisher).publishEvent(any(SeatStatusChangedEvent.class));
    }

    @Test
    @DisplayName("Should correctly identify whether seat is held by current user")
    void isSeatHeldByCurrentUser() {
        availableSeat.setStatus(EventSeatStatus.HELD);
        availableSeat.setHeldByUsername("alice");
        availableSeat.setHeldExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(redisTemplateProvider.getIfAvailable()).thenReturn(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("seat_hold:10:100")).thenReturn("alice");

        boolean result = seatHoldService.isSeatHeldByCurrentUser(10L, 100L, "alice");
        assertThat(result).isTrue();

        when(valueOperations.get("seat_hold:10:100")).thenReturn("bob");
        boolean resultBob = seatHoldService.isSeatHeldByCurrentUser(10L, 100L, "alice");
        assertThat(resultBob).isFalse();
    }
}
