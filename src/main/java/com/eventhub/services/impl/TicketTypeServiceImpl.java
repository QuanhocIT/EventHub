package com.eventhub.services.impl;

import com.eventhub.dto.request.CreateTicketTypeRequest;
import com.eventhub.dto.response.TicketTypeResponse;
import com.eventhub.entities.Event;
import com.eventhub.entities.TicketType;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ForbiddenException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.TicketTypeRepository;
import com.eventhub.services.TicketTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public TicketTypeResponse createTicketType(Long eventId, CreateTicketTypeRequest request, String currentUsername, boolean isAdmin) {
        log.info("Creating ticket type: '{}' for event ID: {} by user: {}", request.getName(), eventId, currentUsername);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        boolean isOwner = event.getOrganizer() != null && event.getOrganizer().getUsername().equals(currentUsername);
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You do not have permission to add ticket types to this event");
        }

        if (request.getSaleStartTime() != null && request.getSaleEndTime() != null) {
            if (request.getSaleStartTime().isAfter(request.getSaleEndTime())) {
                throw new BadRequestException("Sale start time must be before sale end time");
            }
        }

        if (request.getMinPerOrder() != null && request.getMaxPerOrder() != null) {
            if (request.getMinPerOrder() > request.getMaxPerOrder()) {
                throw new BadRequestException("minPerOrder cannot be greater than maxPerOrder");
            }
        }

        TicketType ticketType = TicketType.builder()
                .event(event)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .totalQuantity(request.getTotalQuantity())
                .availableQuantity(request.getTotalQuantity())
                .saleStartTime(request.getSaleStartTime())
                .saleEndTime(request.getSaleEndTime())
                .minPerOrder(request.getMinPerOrder() != null ? request.getMinPerOrder() : 1)
                .maxPerOrder(request.getMaxPerOrder() != null ? request.getMaxPerOrder() : 10)
                .isActive(true)
                .build();

        TicketType saved = ticketTypeRepository.save(ticketType);
        log.info("Created ticket type with ID: {} for event ID: {}", saved.getId(), eventId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketTypeResponse> getTicketTypesByEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event", "id", eventId);
        }
        return ticketTypeRepository.findByEventIdAndIsActiveTrue(eventId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketTypeResponse getTicketTypeById(Long eventId, Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findByIdAndEventId(ticketTypeId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketType", "id", ticketTypeId));
        return mapToResponse(ticketType);
    }

    @Override
    @Transactional
    public TicketTypeResponse updateTicketType(Long eventId, Long ticketTypeId, com.eventhub.dto.request.UpdateTicketTypeRequest request, String currentUsername, boolean isAdmin) {
        log.info("Updating ticket type ID: {} for event ID: {} by user: {}", ticketTypeId, eventId, currentUsername);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        boolean isOwner = event.getOrganizer() != null && event.getOrganizer().getUsername().equals(currentUsername);
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You do not have permission to modify ticket types for this event");
        }

        TicketType ticketType = ticketTypeRepository.findByIdAndEventId(ticketTypeId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketType", "id", ticketTypeId));

        if (request.getSaleStartTime() != null && request.getSaleEndTime() != null) {
            if (request.getSaleStartTime().isAfter(request.getSaleEndTime())) {
                throw new BadRequestException("Sale start time must be before sale end time");
            }
        }

        int soldCount = ticketType.getTotalQuantity() - ticketType.getAvailableQuantity();
        if (request.getTotalQuantity() < soldCount) {
            throw new BadRequestException("Total quantity cannot be less than already sold tickets (" + soldCount + ")");
        }

        ticketType.setName(request.getName());
        ticketType.setDescription(request.getDescription());
        ticketType.setPrice(request.getPrice());
        ticketType.setTotalQuantity(request.getTotalQuantity());
        ticketType.setAvailableQuantity(request.getTotalQuantity() - soldCount);
        ticketType.setSaleStartTime(request.getSaleStartTime());
        ticketType.setSaleEndTime(request.getSaleEndTime());
        if (request.getMinPerOrder() != null) ticketType.setMinPerOrder(request.getMinPerOrder());
        if (request.getMaxPerOrder() != null) ticketType.setMaxPerOrder(request.getMaxPerOrder());
        if (request.getIsActive() != null) ticketType.setIsActive(request.getIsActive());

        TicketType saved = ticketTypeRepository.save(ticketType);
        log.info("Updated ticket type ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteTicketType(Long eventId, Long ticketTypeId, String currentUsername, boolean isAdmin) {
        log.info("Deleting ticket type ID: {} for event ID: {} by user: {}", ticketTypeId, eventId, currentUsername);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        boolean isOwner = event.getOrganizer() != null && event.getOrganizer().getUsername().equals(currentUsername);
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You do not have permission to delete ticket types for this event");
        }

        TicketType ticketType = ticketTypeRepository.findByIdAndEventId(ticketTypeId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketType", "id", ticketTypeId));

        int soldCount = ticketType.getTotalQuantity() - ticketType.getAvailableQuantity();
        if (soldCount > 0) {
            ticketType.setIsActive(false);
            ticketTypeRepository.save(ticketType);
            log.info("Ticket type ID: {} has {} sold tickets, soft-deleted by setting isActive=false", ticketTypeId, soldCount);
        } else {
            ticketTypeRepository.delete(ticketType);
            log.info("Deleted ticket type ID: {}", ticketTypeId);
        }
    }

    private TicketTypeResponse mapToResponse(TicketType entity) {
        return TicketTypeResponse.builder()
                .id(entity.getId())
                .eventId(entity.getEvent().getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .totalQuantity(entity.getTotalQuantity())
                .availableQuantity(entity.getAvailableQuantity())
                .saleStartTime(entity.getSaleStartTime())
                .saleEndTime(entity.getSaleEndTime())
                .minPerOrder(entity.getMinPerOrder())
                .maxPerOrder(entity.getMaxPerOrder())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
