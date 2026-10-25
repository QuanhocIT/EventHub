package com.eventhub.jobs;

import com.eventhub.entities.Booking;
import com.eventhub.entities.BookingItem;
import com.eventhub.entities.Event;
import com.eventhub.entities.EventSeat;
import com.eventhub.entities.Seat;
import com.eventhub.entities.TicketType;
import com.eventhub.enums.BookingStatus;
import com.eventhub.enums.EventSeatStatus;
import com.eventhub.enums.SeatZone;
import com.eventhub.repositories.BookingRepository;
import com.eventhub.repositories.EventSeatRepository;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingCleanupJobTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventSeatRepository eventSeatRepository;

    @Mock
    private ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private BookingCleanupJob bookingCleanupJob;

    private Event event;
    private TicketType ticketType;
    private EventSeat eventSeat;
    private Booking expiredBooking;

    @BeforeEach
    void setUp() {
        event = Event.builder().id(1L).title("Concert").build();

        ticketType = TicketType.builder()
                .id(10L)
                .name("VIP")
                .availableQuantity(40)
                .build();

        Seat seat = Seat.builder().seatRow("A").seatNumber(1).zone(SeatZone.VIP).build();
        eventSeat = EventSeat.builder()
                .id(20L)
                .event(event)
                .seat(seat)
                .status(EventSeatStatus.HELD)
                .build();

        expiredBooking = Booking.builder()
                .id(100L)
                .bookingCode("EVH-20261021-0001")
                .event(event)
                .status(BookingStatus.PENDING_PAYMENT)
                .expiresAt(LocalDateTime.now().minusMinutes(5))
                .build();

        BookingItem item1 = BookingItem.builder()
                .booking(expiredBooking)
                .ticketType(ticketType)
                .quantity(2)
                .build();

        BookingItem item2 = BookingItem.builder()
                .booking(expiredBooking)
                .eventSeat(eventSeat)
                .quantity(1)
                .build();

        expiredBooking.addItem(item1);
        expiredBooking.addItem(item2);
    }

    @Test
    @DisplayName("Should expire overdue bookings and restore ticket inventory and seat availability")
    void cleanupExpiredBookings_Success() {
        when(bookingRepository.findByStatusAndExpiresAtBefore(eq(BookingStatus.PENDING_PAYMENT), any(LocalDateTime.class)))
                .thenReturn(List.of(expiredBooking));
        when(redisTemplateProvider.getIfAvailable()).thenReturn(redisTemplate);

        bookingCleanupJob.cleanupExpiredBookings();

        assertThat(expiredBooking.getStatus()).isEqualTo(BookingStatus.EXPIRED);
        assertThat(ticketType.getAvailableQuantity()).isEqualTo(42); // 40 + 2 restored
        assertThat(eventSeat.getStatus()).isEqualTo(EventSeatStatus.AVAILABLE); // restored
        verify(redisTemplate).delete("seat_hold:1:20");
    }

    @Test
    @DisplayName("Should do nothing when no expired bookings found")
    void cleanupExpiredBookings_NoExpired_DoesNothing() {
        when(bookingRepository.findByStatusAndExpiresAtBefore(eq(BookingStatus.PENDING_PAYMENT), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        bookingCleanupJob.cleanupExpiredBookings();

        verify(redisTemplateProvider, never()).getIfAvailable();
    }

    @Test
    @DisplayName("Should cleanup expired seat holds and reset to AVAILABLE")
    void cleanupExpiredSeatHolds_Success() {
        eventSeat.setHeldByUsername("test_user");
        eventSeat.setHeldExpiresAt(LocalDateTime.now().minusMinutes(2));

        when(eventSeatRepository.findByStatusAndHeldExpiresAtBefore(eq(EventSeatStatus.HELD), any(LocalDateTime.class)))
                .thenReturn(List.of(eventSeat));
        when(redisTemplateProvider.getIfAvailable()).thenReturn(redisTemplate);

        bookingCleanupJob.cleanupExpiredSeatHolds();

        assertThat(eventSeat.getStatus()).isEqualTo(EventSeatStatus.AVAILABLE);
        assertThat(eventSeat.getHeldByUsername()).isNull();
        assertThat(eventSeat.getHeldExpiresAt()).isNull();
        verify(redisTemplate).delete("seat_hold:1:20");
    }
}
