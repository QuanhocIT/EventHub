package com.eventhub.listeners;

import com.eventhub.events.EventCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EventNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(EventNotificationListener.class);

    @EventListener
    public void onEventCreated(EventCreatedEvent event) {
        log.info("[Domain Event] Event created successfully - ID: {}, Title: '{}' at {}",
                event.getEventId(), event.getTitle(), event.getEventTime());
        // Placeholder for sending push notifications, emails, or dispatching to message queue
    }
}
