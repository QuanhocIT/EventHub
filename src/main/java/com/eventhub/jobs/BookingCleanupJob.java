package com.eventhub.jobs;

import com.eventhub.entities.Booking;
import com.eventhub.entities.BookingItem;
import com.eventhub.entities.EventSeat;
import com.eventhub.entities.TicketType;
import com.eventhub.enums.BookingStatus;
import com.eventhub.enums.EventSeatStatus;
import com.eventhub.events.SeatStatusChangedEvent;
import com.eventhub.repositories.BookingRepository;
import com.eventhub.repositories.EventSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCleanupJob {

    private final BookingRepository bookingRepository;
    private final EventSeatRepository eventSeatRepository;
    private final ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(BookingStatus.PENDING_PAYMENT, now);

        if (expiredBookings.isEmpty()) {
            return;
        }

        log.info("Found {} expired pending bookings. Cleaning up and releasing inventory...", expiredBookings.size());
        RedisTemplate<String, Object> redisTemplate = redisTemplateProvider.getIfAvailable();

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.EXPIRED);

            for (BookingItem item : booking.getItems()) {
                // Restore ticket stock
                if (item.getTicketType() != null) {
                    TicketType ticketType = item.getTicketType();
                    ticketType.setAvailableQuantity(ticketType.getAvailableQuantity() + item.getQuantity());
                }

                // Release seat back to AVAILABLE
                if (item.getEventSeat() != null) {
                    EventSeat eventSeat = item.getEventSeat();
                    eventSeat.setStatus(EventSeatStatus.AVAILABLE);
                    eventSeat.setHeldByUsername(null);
                    eventSeat.setHeldExpiresAt(null);

                    // Evict Redis hold key if still present
                    if (redisTemplate != null) {
                        String redisKey = String.format("seat_hold:%d:%d",
                                booking.getEvent().getId(), eventSeat.getId());
                        redisTemplate.delete(redisKey);
                    }
                }
            }
            log.info("Booking {} marked as EXPIRED and items released", booking.getBookingCode());
        }
    }

    @Scheduled(fixedRate = 30000) // Runs every 30 seconds for real-time seat hold expiration
    @Transactional
    public void cleanupExpiredSeatHolds() {
        LocalDateTime now = LocalDateTime.now();
        List<EventSeat> expiredSeats = eventSeatRepository.findByStatusAndHeldExpiresAtBefore(EventSeatStatus.HELD, now);

        if (expiredSeats.isEmpty()) {
            return;
        }

        log.info("Found {} expired seat holds. Releasing back to AVAILABLE...", expiredSeats.size());
        RedisTemplate<String, Object> redisTemplate = redisTemplateProvider.getIfAvailable();
        List<Long> releasedIds = new ArrayList<>();

        for (EventSeat seat : expiredSeats) {
            seat.setStatus(EventSeatStatus.AVAILABLE);
            seat.setHeldByUsername(null);
            seat.setHeldExpiresAt(null);
            releasedIds.add(seat.getId());

            if (redisTemplate != null) {
                String redisKey = String.format("seat_hold:%d:%d", seat.getEvent().getId(), seat.getId());
                redisTemplate.delete(redisKey);
            }
        }

        log.info("Successfully auto-released {} expired seat holds back to AVAILABLE: {}", releasedIds.size(), releasedIds);
    }
}
