package com.eventhub.mappers;

import com.eventhub.dto.request.CreateEventRequest;
import com.eventhub.dto.request.UpdateEventRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.entities.Event;
import com.eventhub.enums.EventStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EventMapper {

    public Event toEntity(CreateEventRequest request) {
        if (request == null) {
            return null;
        }

        return Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .capacity(request.getCapacity())
                .price(request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO)
                .status(request.getStatus() != null ? request.getStatus() : EventStatus.DRAFT)
                .build();
    }

    public EventResponse toResponse(Event event) {
        if (event == null) {
            return null;
        }

        Long organizerId = event.getOrganizer() != null ? event.getOrganizer().getId() : null;
        String organizerName = null;
        if (event.getOrganizer() != null) {
            organizerName = event.getOrganizer().getFullName() != null && !event.getOrganizer().getFullName().isBlank()
                    ? event.getOrganizer().getFullName()
                    : event.getOrganizer().getUsername();
        }

        Long categoryId = event.getCategory() != null ? event.getCategory().getId() : null;
        String categoryName = event.getCategory() != null ? event.getCategory().getName() : null;
        String categorySlug = event.getCategory() != null ? event.getCategory().getSlug() : null;

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .capacity(event.getCapacity())
                .price(event.getPrice())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .organizerId(organizerId)
                .organizerName(organizerName)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .categorySlug(categorySlug)
                .averageRating(event.getAverageRating() != null ? event.getAverageRating() : 0.0)
                .reviewCount(event.getReviewCount() != null ? event.getReviewCount() : 0)
                .build();
    }

    public void updateEntityFromRequest(UpdateEventRequest request, Event event) {
        if (request == null || event == null) {
            return;
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setCapacity(request.getCapacity());
        if (request.getPrice() != null) {
            event.setPrice(request.getPrice());
        }
        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }
    }
}
