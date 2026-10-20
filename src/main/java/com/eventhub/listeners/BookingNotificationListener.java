package com.eventhub.listeners;

import com.eventhub.events.BookingConfirmedEvent;
import com.eventhub.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingNotificationListener {

    private final EmailService emailService;

    @EventListener
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        log.info("[Listener] Handling BookingConfirmedEvent for booking: {}", event.getBookingCode());

        String amountFormatted = event.getTotalAmount() != null
                ? String.format("%,.0f", event.getTotalAmount().doubleValue())
                : "0";

        emailService.sendBookingConfirmation(
                event.getAttendeeEmail(),
                event.getAttendeeName(),
                event.getBookingCode(),
                event.getEventTitle(),
                amountFormatted
        );
    }
}
