package com.eventhub.services.impl;

import com.eventhub.dto.response.CheckInResponse;
import com.eventhub.dto.response.TicketResponse;
import com.eventhub.entities.Booking;
import com.eventhub.entities.BookingItem;
import com.eventhub.entities.Ticket;
import com.eventhub.entities.User;
import com.eventhub.enums.TicketStatus;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ForbiddenException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.TicketRepository;
import com.eventhub.repositories.UserRepository;
import com.eventhub.services.QrCodeService;
import com.eventhub.services.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final QrCodeService qrCodeService;

    @Override
    @Transactional
    public List<Ticket> generateTicketsForBooking(Booking booking) {
        log.info("Generating tickets for confirmed booking: {}", booking.getBookingCode());

        List<Ticket> tickets = new ArrayList<>();
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        for (BookingItem item : booking.getItems()) {
            int quantity = item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1;

            for (int i = 0; i < quantity; i++) {
                String randomCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
                String ticketCode = String.format("TK-%s-%s", datePrefix, randomCode);
                String qrData = String.format("eventhub://tickets/%s?event=%d", ticketCode, booking.getEvent().getId());

                Ticket ticket = Ticket.builder()
                        .ticketCode(ticketCode)
                        .booking(booking)
                        .bookingItem(item)
                        .event(booking.getEvent())
                        .ticketType(item.getTicketType())
                        .eventSeat(item.getEventSeat())
                        .status(TicketStatus.VALID)
                        .qrCodeData(qrData)
                        .build();

                tickets.add(ticket);
            }
        }

        List<Ticket> savedTickets = ticketRepository.saveAll(tickets);
        log.info("Successfully generated {} tickets for booking: {}", savedTickets.size(), booking.getBookingCode());
        return savedTickets;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getMyTickets(String currentUsername, Pageable pageable) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUsername));

        return ticketRepository.findByBookingUserId(user.getId(), pageable)
                .map(ticket -> mapToResponse(ticket, true));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketByCode(String ticketCode, String currentUsername, boolean isStaffOrAdmin) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "code", ticketCode));

        boolean isOwner = ticket.getBooking().getUser().getUsername().equals(currentUsername);
        if (!isOwner && !isStaffOrAdmin) {
            throw new ForbiddenException("You do not have permission to view this ticket");
        }

        return mapToResponse(ticket, true);
    }

    @Override
    @Transactional
    public CheckInResponse checkInTicket(String ticketCode, String staffUsername) {
        log.info("Processing gate check-in for ticket: {} by staff: {}", ticketCode, staffUsername);

        User staffUser = userRepository.findByUsername(staffUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", staffUsername));

        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "code", ticketCode));

        if (ticket.getStatus() == TicketStatus.USED) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
            String checkInStr = ticket.getCheckedInAt() != null ? ticket.getCheckedInAt().format(formatter) : "earlier";
            throw new BadRequestException(String.format("Ticket %s was already used for check-in at %s", ticketCode, checkInStr));
        }

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new BadRequestException(String.format("Ticket %s has been cancelled and cannot be used", ticketCode));
        }

        ticket.setStatus(TicketStatus.USED);
        ticket.setCheckedInAt(LocalDateTime.now());
        ticket.setCheckedInBy(staffUser);

        Ticket updated = ticketRepository.save(ticket);
        log.info("Ticket {} successfully checked in at {} by staff: {}", ticketCode, updated.getCheckedInAt(), staffUsername);

        String seatInfo = null;
        if (updated.getEventSeat() != null && updated.getEventSeat().getSeat() != null) {
            seatInfo = String.format("Row %s - Seat %s (%s)",
                    updated.getEventSeat().getSeat().getSeatRow(),
                    updated.getEventSeat().getSeat().getSeatNumber(),
                    updated.getEventSeat().getSeat().getZone());
        }

        return CheckInResponse.builder()
                .success(true)
                .message("Check-in successful. Welcome to the event!")
                .ticketCode(updated.getTicketCode())
                .attendeeName(updated.getBooking().getUser().getFullName())
                .eventTitle(updated.getEvent().getTitle())
                .seatInfo(seatInfo)
                .ticketTypeName(updated.getTicketType() != null ? updated.getTicketType().getName() : "General Admission")
                .checkInTime(updated.getCheckedInAt())
                .staffUsername(staffUsername)
                .build();
    }

    private TicketResponse mapToResponse(Ticket ticket, boolean includeQrCode) {
        String seatInfo = null;
        if (ticket.getEventSeat() != null && ticket.getEventSeat().getSeat() != null) {
            seatInfo = String.format("Row %s - Seat %s (%s)",
                    ticket.getEventSeat().getSeat().getSeatRow(),
                    ticket.getEventSeat().getSeat().getSeatNumber(),
                    ticket.getEventSeat().getSeat().getZone());
        }

        String qrBase64 = null;
        if (includeQrCode && ticket.getQrCodeData() != null) {
            qrBase64 = qrCodeService.generateQrCodeBase64(ticket.getQrCodeData(), 250, 250);
        }

        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketCode(ticket.getTicketCode())
                .bookingCode(ticket.getBooking().getBookingCode())
                .eventId(ticket.getEvent().getId())
                .eventTitle(ticket.getEvent().getTitle())
                .attendeeName(ticket.getBooking().getUser().getFullName())
                .ticketTypeName(ticket.getTicketType() != null ? ticket.getTicketType().getName() : "General Admission")
                .seatInfo(seatInfo)
                .status(ticket.getStatus())
                .qrCodeBase64(qrBase64)
                .checkedInAt(ticket.getCheckedInAt())
                .checkedInByStaff(ticket.getCheckedInBy() != null ? ticket.getCheckedInBy().getUsername() : null)
                .createdAt(ticket.getCreatedAt())
                .build();
    }
}
