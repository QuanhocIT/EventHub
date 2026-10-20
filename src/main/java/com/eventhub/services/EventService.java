package com.eventhub.services;

import com.eventhub.common.PageResponse;
import com.eventhub.dto.request.CreateEventRequest;
import com.eventhub.dto.request.UpdateEventRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.enums.EventStatus;
import org.springframework.data.domain.Pageable;

public interface EventService {

    EventResponse createEvent(CreateEventRequest request, String organizerUsername);

    EventResponse getEventById(Long id);

    PageResponse<EventResponse> getAllEvents(String keyword, EventStatus status, Pageable pageable);

    EventResponse updateEvent(Long id, UpdateEventRequest request, String currentUsername, boolean isAdmin);

    void deleteEvent(Long id, String currentUsername, boolean isAdmin);

    java.util.List<com.eventhub.dto.response.EventSeatResponse> getEventSeats(Long eventId);

    java.util.List<com.eventhub.dto.response.EventSeatResponse> assignVenueToEvent(
            Long eventId,
            Long venueId,
            java.math.BigDecimal basePrice,
            String currentUsername,
            boolean isAdmin
    );
}
