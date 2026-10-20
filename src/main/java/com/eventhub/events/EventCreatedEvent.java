package com.eventhub.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class EventCreatedEvent extends ApplicationEvent {

    private final Long eventId;
    private final String title;
    private final LocalDateTime eventTime;

    public EventCreatedEvent(Object source, Long eventId, String title) {
        super(source);
        this.eventId = eventId;
        this.title = title;
        this.eventTime = LocalDateTime.now();
    }
}
