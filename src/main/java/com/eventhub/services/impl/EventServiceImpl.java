package com.eventhub.services.impl;

import com.eventhub.common.PageResponse;
import com.eventhub.dto.request.CreateEventRequest;
import com.eventhub.dto.request.UpdateEventRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.entities.Event;
import com.eventhub.entities.User;
import com.eventhub.enums.EventStatus;
import com.eventhub.events.EventCreatedEvent;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.mappers.EventMapper;
import com.eventhub.repositories.EventRepository;
import com.eventhub.dto.response.EventSeatResponse;
import com.eventhub.entities.EventSeat;
import com.eventhub.entities.Seat;
import com.eventhub.entities.Venue;
import com.eventhub.enums.EventSeatStatus;
import com.eventhub.enums.SeatZone;
import com.eventhub.repositories.EventSeatRepository;
import com.eventhub.repositories.SeatRepository;
import com.eventhub.repositories.UserRepository;
import com.eventhub.repositories.VenueRepository;
import com.eventhub.services.EventService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final EventSeatRepository eventSeatRepository;
    private final EventMapper eventMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public EventResponse createEvent(CreateEventRequest request, String organizerUsername) {
        log.info("Creating event with title: {} by organizer: {}", request.getTitle(), organizerUsername);

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("Event end time cannot be before start time");
        }

        User organizer = userRepository.findByUsername(organizerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", organizerUsername));

        Event event = eventMapper.toEntity(request);
        event.setOrganizer(organizer);
        Event savedEvent = eventRepository.save(event);

        // Publish domain event
        eventPublisher.publishEvent(new EventCreatedEvent(this, savedEvent.getId(), savedEvent.getTitle()));

        log.info("Event created successfully with ID: {}", savedEvent.getId());
        return eventMapper.toResponse(savedEvent);
    }

    @Override
    public EventResponse getEventById(Long id) {
        log.debug("Fetching event with ID: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        return eventMapper.toResponse(event);
    }

    @Override
    public PageResponse<EventResponse> getAllEvents(String keyword, EventStatus status, Pageable pageable) {
        log.debug("Searching events - keyword: {}, status: {}, pageable: {}", keyword, status, pageable);
        Page<Event> page = eventRepository.searchEvents(keyword, status, pageable);
        Page<EventResponse> responsePage = page.map(eventMapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long id, UpdateEventRequest request, String currentUsername, boolean isAdmin) {
        log.info("Updating event with ID: {} by user: {} (isAdmin: {})", id, currentUsername, isAdmin);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        boolean isOwner = event.getOrganizer() != null && event.getOrganizer().getUsername().equals(currentUsername);
        if (!isAdmin && !isOwner) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to modify this event");
        }

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("Event end time cannot be before start time");
        }

        eventMapper.updateEntityFromRequest(request, event);
        Event updatedEvent = eventRepository.save(event);

        log.info("Event updated successfully with ID: {}", updatedEvent.getId());
        return eventMapper.toResponse(updatedEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id, String currentUsername, boolean isAdmin) {
        log.info("Deleting event with ID: {} by user: {} (isAdmin: {})", id, currentUsername, isAdmin);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        boolean isOwner = event.getOrganizer() != null && event.getOrganizer().getUsername().equals(currentUsername);
        if (!isAdmin && !isOwner) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to delete this event");
        }

        eventRepository.delete(event);
        log.info("Event deleted successfully with ID: {}", id);
    }

    @Override
    public List<EventSeatResponse> getEventSeats(Long eventId) {
        log.debug("Fetching seats for event ID: {}", eventId);
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event", "id", eventId);
        }

        List<EventSeat> eventSeats = eventSeatRepository.findByEventIdOrderBySeatSeatRowAscSeatSeatNumberAsc(eventId);
        return eventSeats.stream()
                .map(es -> EventSeatResponse.builder()
                        .id(es.getId())
                        .seatId(es.getSeat().getId())
                        .seatRow(es.getSeat().getSeatRow())
                        .seatNumber(es.getSeat().getSeatNumber())
                        .zone(es.getSeat().getZone())
                        .status(es.getStatus())
                        .price(es.getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<EventSeatResponse> assignVenueToEvent(
            Long eventId,
            Long venueId,
            BigDecimal basePrice,
            String currentUsername,
            boolean isAdmin
    ) {
        log.info("Assigning venue ID: {} to event ID: {} by user: {}", venueId, eventId, currentUsername);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        boolean isOwner = event.getOrganizer() != null && event.getOrganizer().getUsername().equals(currentUsername);
        if (!isAdmin && !isOwner) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to modify this event");
        }

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", venueId));

        event.setVenue(venue);
        event.setLocation(venue.getName() + ", " + venue.getAddress());

        List<Seat> physicalSeats = seatRepository.findByVenueIdOrderBySeatRowAscSeatNumberAsc(venueId);
        if (physicalSeats.isEmpty()) {
            throw new BadRequestException("Venue does not have any configured seats");
        }

        // Delete existing event seats if any
        List<EventSeat> existing = eventSeatRepository.findByEventIdOrderBySeatSeatRowAscSeatSeatNumberAsc(eventId);
        if (!existing.isEmpty()) {
            eventSeatRepository.deleteAll(existing);
        }

        BigDecimal price = basePrice != null ? basePrice : (event.getPrice() != null ? event.getPrice() : BigDecimal.valueOf(50));

        List<EventSeat> eventSeats = physicalSeats.stream()
                .map(seat -> {
                    BigDecimal seatPrice = price;
                    if (seat.getZone() == SeatZone.VIP) {
                        seatPrice = price.multiply(BigDecimal.valueOf(1.5));
                    }
                    return EventSeat.builder()
                            .event(event)
                            .seat(seat)
                            .status(EventSeatStatus.AVAILABLE)
                            .price(seatPrice)
                            .build();
                })
                .collect(Collectors.toList());

        List<EventSeat> savedEventSeats = eventSeatRepository.saveAll(eventSeats);
        event.setCapacity(savedEventSeats.size());
        eventRepository.save(event);

        log.info("Assigned venue successfully, generated {} seats for event ID: {}", savedEventSeats.size(), eventId);
        return getEventSeats(eventId);
    }
}
