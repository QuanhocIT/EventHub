package com.eventhub.listeners;

import com.eventhub.events.BookingConfirmedEvent;
import com.eventhub.services.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingNotificationListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BookingNotificationListener listener;

    @Test
    @DisplayName("Should receive BookingConfirmedEvent and trigger EmailService")
    void onBookingConfirmed_Success() {
        BookingConfirmedEvent event = BookingConfirmedEvent.builder()
                .bookingId(1L)
                .bookingCode("EVH-20261021-0001")
                .attendeeEmail("alice@example.com")
                .attendeeName("Alice")
                .eventTitle("Concert 2026")
                .totalAmount(new BigDecimal("1500000"))
                .build();

        when(emailService.sendBookingConfirmation(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(true));

        listener.onBookingConfirmed(event);

        verify(emailService).sendBookingConfirmation(
                eq("alice@example.com"),
                eq("Alice"),
                eq("EVH-20261021-0001"),
                eq("Concert 2026"),
                anyString()
        );
    }
}
