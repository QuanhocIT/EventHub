package com.eventhub.controllers;

import com.eventhub.events.SeatStatusChangedEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Tag(name = "Seat Stream", description = "Server-Sent Events (SSE) for real-time seat status updates")
@RestController
@RequestMapping("/api/v1/events")
public class SeatStreamController {

    // eventId -> list of active SSE emitters
    private final Map<Long, List<SseEmitter>> eventEmitters = new ConcurrentHashMap<>();

    @Operation(summary = "Stream real-time seat status updates via SSE")
    @GetMapping(value = "/{id}/seats/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSeatUpdates(@PathVariable Long id) {
        // 10 minutes timeout per SSE connection
        SseEmitter emitter = new SseEmitter(600_000L);

        eventEmitters.computeIfAbsent(id, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(id, emitter));
        emitter.onTimeout(() -> removeEmitter(id, emitter));
        emitter.onError(e -> removeEmitter(id, emitter));

        try {
            // Initial heartbeat / connected message
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data(Map.of("eventId", id, "message", "Connected to real-time seat stream")));
        } catch (IOException e) {
            removeEmitter(id, emitter);
        }

        return emitter;
    }

    @EventListener
    public void onSeatStatusChanged(SeatStatusChangedEvent event) {
        List<SseEmitter> emitters = eventEmitters.get(event.getEventId());
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        Map<String, Object> payload = Map.of(
                "eventId", event.getEventId(),
                "action", event.getAction(),
                "seatIds", event.getSeatIds(),
                "heldByUsername", event.getHeldByUsername() != null ? event.getHeldByUsername() : "",
                "timestamp", event.getTimestamp().toString()
        );

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("SEAT_UPDATE")
                        .data(payload));
            } catch (Exception e) {
                removeEmitter(event.getEventId(), emitter);
            }
        }
    }

    private void removeEmitter(Long eventId, SseEmitter emitter) {
        List<SseEmitter> emitters = eventEmitters.get(eventId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                eventEmitters.remove(eventId);
            }
        }
    }
}
