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
import com.eventhub.dto.request.BatchUpdateEventSeatsRequest;
import com.eventhub.dto.request.ConfigureEventSeatsRequest;
import com.eventhub.dto.request.EventSeatConfigItemRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
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
    private final com.eventhub.repositories.CategoryRepository categoryRepository;
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

        if (request.getCategoryId() != null) {
            com.eventhub.entities.Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            event.setCategory(category);
        }

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
        return getAllEvents(keyword, null, status, null, null, null, null, null, pageable);
    }

    @Override
    public PageResponse<EventResponse> getAllEvents(
            String keyword,
            Long categoryId,
            EventStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate,
            String location,
            Pageable pageable
    ) {
        log.debug("Searching events with filters - keyword: {}, categoryId: {}, status: {}, minPrice: {}, maxPrice: {}",
                keyword, categoryId, status, minPrice, maxPrice);

        org.springframework.data.jpa.domain.Specification<Event> spec =
                com.eventhub.repositories.specifications.EventSpecification.filter(
                        keyword, categoryId, status, minPrice, maxPrice, startDate, endDate, location);

        Page<Event> page = eventRepository.findAll(spec, pageable);
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

        if (request.getCategoryId() != null) {
            com.eventhub.entities.Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            event.setCategory(category);
        }

        Event updatedEvent = eventRepository.save(event);

        log.info("Event updated successfully with ID: {}", updatedEvent.getId());
        return eventMapper.toResponse(updatedEvent);
    }

    @Override
    @Transactional
    public EventResponse updateEventStatus(Long id, EventStatus status, String currentUsername, boolean isAdmin) {
        log.info("Updating event status with ID: {} to {} by user: {} (isAdmin: {})", id, status, currentUsername, isAdmin);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        boolean isOwner = event.getOrganizer() != null && event.getOrganizer().getUsername().equals(currentUsername);
        if (!isAdmin && !isOwner) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to modify this event");
        }

        event.setStatus(status);
        Event updatedEvent = eventRepository.save(event);

        log.info("Event status updated successfully with ID: {} to {}", updatedEvent.getId(), status);
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

        String currentUsername = null;
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            currentUsername = auth.getName();
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        List<EventSeat> eventSeats = eventSeatRepository.findByEventIdOrderBySeatSeatRowAscSeatSeatNumberAsc(eventId);

        final String finalCurrentUsername = currentUsername;
        return eventSeats.stream()
                .map(es -> {
                    EventSeatStatus status = es.getStatus();
                    boolean isHeldByCurrentUser = false;
                    Long remainingSeconds = null;

                    if (status == EventSeatStatus.HELD) {
                        if (es.getHeldExpiresAt() != null && es.getHeldExpiresAt().isBefore(now)) {
                            status = EventSeatStatus.AVAILABLE;
                        } else {
                            isHeldByCurrentUser = finalCurrentUsername != null && finalCurrentUsername.equals(es.getHeldByUsername());
                            if (es.getHeldExpiresAt() != null) {
                                remainingSeconds = Math.max(0, java.time.Duration.between(now, es.getHeldExpiresAt()).getSeconds());
                            }
                        }
                    }

                    return EventSeatResponse.builder()
                            .id(es.getId())
                            .seatId(es.getSeat().getId())
                            .seatRow(es.getSeat().getSeatRow())
                            .seatNumber(es.getSeat().getSeatNumber())
                            .zone(es.getSeat().getZone())
                            .status(status)
                            .price(es.getPrice())
                            .heldByCurrentUser(isHeldByCurrentUser)
                            .heldExpiresAt(status == EventSeatStatus.HELD ? es.getHeldExpiresAt() : null)
                            .remainingHoldSeconds(remainingSeconds)
                            .build();
                })
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

    @Override
    @Transactional
    public List<EventSeatResponse> configureEventSeats(
            Long eventId,
            ConfigureEventSeatsRequest request,
            String currentUsername,
            boolean isAdmin
    ) {
        log.info("Configuring visual seat map for event ID: {} by user: {}", eventId, currentUsername);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        boolean isOwner = event.getOrganizer() != null && event.getOrganizer().getUsername().equals(currentUsername);
        if (!isAdmin && !isOwner) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to modify this event");
        }

        Venue venue = null;
        if (request.getVenueId() != null) {
            venue = venueRepository.findById(request.getVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", request.getVenueId()));
            event.setVenue(venue);
        } else if (event.getVenue() != null) {
            venue = event.getVenue();
        }

        // If event doesn't have a venue, create or reuse a dedicated event venue
        if (venue == null) {
            venue = venueRepository.save(Venue.builder()
                    .name("Hall - " + event.getTitle())
                    .address(event.getLocation() != null ? event.getLocation() : "Default Arena")
                    .city("Vietnam")
                    .capacity(request.getSeats().size())
                    .build());
            event.setVenue(venue);
        }

        // Clear existing event seats
        List<EventSeat> existingEventSeats = eventSeatRepository.findByEventIdOrderBySeatSeatRowAscSeatSeatNumberAsc(eventId);
        if (!existingEventSeats.isEmpty()) {
            eventSeatRepository.deleteAll(existingEventSeats);
            eventSeatRepository.flush();
        }

        // Fetch or create backing physical seats
        List<Seat> physicalSeats = seatRepository.findByVenueIdOrderBySeatRowAscSeatNumberAsc(venue.getId());
        Map<String, Seat> seatMap = physicalSeats.stream()
                .collect(Collectors.toMap(s -> s.getSeatRow() + "-" + s.getSeatNumber(), s -> s, (s1, s2) -> s1));

        List<EventSeat> newEventSeats = new ArrayList<>();
        BigDecimal defaultPrice = request.getDefaultPrice() != null ? request.getDefaultPrice() : (event.getPrice() != null ? event.getPrice() : BigDecimal.valueOf(50));

        for (EventSeatConfigItemRequest item : request.getSeats()) {
            String row = item.getSeatRow().trim().toUpperCase();
            String key = row + "-" + item.getSeatNumber();
            Seat physicalSeat = seatMap.get(key);

            if (physicalSeat == null) {
                physicalSeat = Seat.builder()
                        .venue(venue)
                        .seatRow(row)
                        .seatNumber(item.getSeatNumber())
                        .zone(item.getZone() != null ? item.getZone() : SeatZone.STANDARD)
                        .build();
                physicalSeat = seatRepository.save(physicalSeat);
                seatMap.put(key, physicalSeat);
            } else if (item.getZone() != null && physicalSeat.getZone() != item.getZone()) {
                physicalSeat.setZone(item.getZone());
                seatRepository.save(physicalSeat);
            }

            BigDecimal seatPrice = item.getPrice() != null ? item.getPrice() : defaultPrice;
            EventSeatStatus status = item.getStatus() != null ? item.getStatus() : EventSeatStatus.AVAILABLE;

            newEventSeats.add(EventSeat.builder()
                    .event(event)
                    .seat(physicalSeat)
                    .status(status)
                    .price(seatPrice)
                    .build());
        }

        List<EventSeat> saved = eventSeatRepository.saveAll(newEventSeats);
        event.setCapacity(saved.size());
        eventRepository.save(event);

        venue.setCapacity(seatRepository.findByVenueIdOrderBySeatRowAscSeatNumberAsc(venue.getId()).size());
        venueRepository.save(venue);

        log.info("Configured visual seat map with {} seats for event ID: {}", saved.size(), eventId);
        return getEventSeats(eventId);
    }

    @Override
    @Transactional
    public List<EventSeatResponse> batchUpdateEventSeats(
            Long eventId,
            BatchUpdateEventSeatsRequest request,
            String currentUsername,
            boolean isAdmin
    ) {
        log.info("Batch updating {} event seats for event ID: {} by user: {}", request.getEventSeatIds().size(), eventId, currentUsername);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        boolean isOwner = event.getOrganizer() != null && event.getOrganizer().getUsername().equals(currentUsername);
        if (!isAdmin && !isOwner) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to modify this event");
        }

        for (Long seatId : request.getEventSeatIds()) {
            EventSeat eventSeat = eventSeatRepository.findById(seatId).orElse(null);
            if (eventSeat != null && eventSeat.getEvent().getId().equals(eventId)) {
                if (request.getPrice() != null) {
                    eventSeat.setPrice(request.getPrice());
                }
                if (request.getStatus() != null) {
                    eventSeat.setStatus(request.getStatus());
                }
                if (request.getZone() != null && eventSeat.getSeat() != null) {
                    eventSeat.getSeat().setZone(request.getZone());
                    seatRepository.save(eventSeat.getSeat());
                }
                eventSeatRepository.save(eventSeat);
            }
        }

        return getEventSeats(eventId);
    }
}
