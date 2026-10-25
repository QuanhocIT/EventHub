package com.eventhub.services;

import com.eventhub.dto.request.SeatHoldRequest;
import com.eventhub.dto.response.SeatHoldResponse;
import com.eventhub.entities.Event;
import com.eventhub.entities.EventSeat;
import com.eventhub.entities.Seat;
import com.eventhub.entities.User;
import com.eventhub.entities.Venue;
import com.eventhub.enums.EventSeatStatus;
import com.eventhub.enums.EventStatus;
import com.eventhub.enums.SeatZone;
import com.eventhub.enums.UserRole;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.EventSeatRepository;
import com.eventhub.repositories.SeatRepository;
import com.eventhub.repositories.UserRepository;
import com.eventhub.repositories.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SeatHoldConcurrencyTest {

    @Autowired
    private SeatHoldService seatHoldService;

    @Autowired
    private EventSeatRepository eventSeatRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    private Long testEventId;
    private Long testSeatId;

    @BeforeEach
    void setUp() {
        Venue venue = venueRepository.save(Venue.builder()
                .name("Concurrency Test Arena")
                .address("123 Test St")
                .city("HCM")
                .capacity(100)
                .build());

        Seat seat = seatRepository.save(Seat.builder()
                .venue(venue)
                .seatRow("Z")
                .seatNumber(99)
                .zone(SeatZone.VIP)
                .build());

        User testUser = userRepository.save(User.builder()
                .username("organizer_race_" + System.currentTimeMillis())
                .password("secret123")
                .fullName("Race Organizer")
                .email("race_" + System.currentTimeMillis() + "@example.com")
                .role(UserRole.ROLE_ORGANIZER)
                .enabled(true)
                .build());

        Event event = eventRepository.save(Event.builder()
                .title("Race Condition Mega Concert")
                .description("High load test event")
                .organizer(testUser)
                .capacity(100)
                .venue(venue)
                .location("HCM")
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(3))
                .price(BigDecimal.valueOf(500000))
                .status(EventStatus.PUBLISHED)
                .build());

        EventSeat eventSeat = eventSeatRepository.save(EventSeat.builder()
                .event(event)
                .seat(seat)
                .price(BigDecimal.valueOf(500000))
                .status(EventSeatStatus.AVAILABLE)
                .build());

        testEventId = event.getId();
        testSeatId = eventSeat.getId();
    }

    @Test
    @DisplayName("Concurrency: 10 threads attempt to hold the exact same seat simultaneously - only 1 succeeds")
    void holdSeat_Concurrency_RaceConditionPrevented() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> successfulUsers = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final String username = "user_" + i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await(); // Wait for all threads to align at the starting line
                    SeatHoldRequest req = SeatHoldRequest.builder()
                            .eventId(testEventId)
                            .eventSeatIds(List.of(testSeatId))
                            .build();

                    SeatHoldResponse response = seatHoldService.holdSeats(req, username);
                    if (response != null && !response.getEventSeatIds().isEmpty()) {
                        successCount.incrementAndGet();
                        successfulUsers.add(username);
                    }
                } catch (Exception ex) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown(); // Fire all 10 threads simultaneously
        doneLatch.await();
        executor.shutdown();

        // Verification: Exactly 1 user held the seat, 9 failed
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);
        assertThat(successfulUsers).hasSize(1);

        EventSeat savedSeat = eventSeatRepository.findById(testSeatId).orElseThrow();
        assertThat(savedSeat.getStatus()).isEqualTo(EventSeatStatus.HELD);
        assertThat(savedSeat.getHeldByUsername()).isEqualTo(successfulUsers.getFirst());
        assertThat(savedSeat.getHeldExpiresAt()).isAfter(LocalDateTime.now());
    }
}
