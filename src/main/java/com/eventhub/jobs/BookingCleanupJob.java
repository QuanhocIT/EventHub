package com.eventhub.jobs;

import com.eventhub.entities.Booking;
import com.eventhub.entities.BookingItem;
import com.eventhub.entities.EventSeat;
import com.eventhub.entities.TicketType;
import com.eventhub.enums.BookingStatus;
import com.eventhub.enums.EventSeatStatus;
import com.eventhub.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCleanupJob {

    private final BookingRepository bookingRepository;
    private final ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider;

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
}
