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

    EventResponse updateEvent(Long id, UpdateEventRequest request);

    void deleteEvent(Long id);
}
