package com.eventhub.services;

import com.eventhub.dto.response.CheckInResponse;
import com.eventhub.dto.response.TicketResponse;
import com.eventhub.entities.Booking;
import com.eventhub.entities.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TicketService {

    List<Ticket> generateTicketsForBooking(Booking booking);

    Page<TicketResponse> getMyTickets(String currentUsername, Pageable pageable);

    TicketResponse getTicketByCode(String ticketCode, String currentUsername, boolean isStaffOrAdmin);

    CheckInResponse checkInTicket(String ticketCode, String staffUsername);
}
