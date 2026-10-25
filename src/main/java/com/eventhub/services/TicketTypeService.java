package com.eventhub.services;

import com.eventhub.dto.request.CreateTicketTypeRequest;
import com.eventhub.dto.request.UpdateTicketTypeRequest;
import com.eventhub.dto.response.TicketTypeResponse;

import java.util.List;

public interface TicketTypeService {

    TicketTypeResponse createTicketType(Long eventId, CreateTicketTypeRequest request, String currentUsername, boolean isAdmin);

    List<TicketTypeResponse> getTicketTypesByEvent(Long eventId);

    TicketTypeResponse getTicketTypeById(Long eventId, Long ticketTypeId);

    TicketTypeResponse updateTicketType(Long eventId, Long ticketTypeId, UpdateTicketTypeRequest request, String currentUsername, boolean isAdmin);

    void deleteTicketType(Long eventId, Long ticketTypeId, String currentUsername, boolean isAdmin);
}
