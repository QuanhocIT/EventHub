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
import com.eventhub.dto.response.GateStatsResponse;
import com.eventhub.dto.response.GateTierStatResponse;
import com.eventhub.entities.Event;
import com.eventhub.entities.TicketType;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.TicketRepository;
import com.eventhub.repositories.TicketTypeRepository;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final QrCodeService qrCodeService;
    private final com.eventhub.services.LoyaltyService loyaltyService;

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
            String staff = ticket.getCheckedInBy() != null ? ticket.getCheckedInBy().getFullName() : "nhân viên";
            throw new BadRequestException(String.format("Cảnh báo Anti-Passback: Ticket %s was already used for check-in at %s bởi %s. Từ chối vào cổng!", ticketCode, checkInStr, staff));
        }

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new BadRequestException(String.format("Ticket %s has been cancelled and cannot be used", ticketCode));
        }

        ticket.setStatus(TicketStatus.USED);
        ticket.setCheckedInAt(LocalDateTime.now());
        ticket.setCheckedInBy(staffUser);

        Ticket updated = ticketRepository.save(ticket);
        log.info("Ticket {} successfully checked in at {} by staff: {}", ticketCode, updated.getCheckedInAt(), staffUsername);

        if (loyaltyService != null && updated.getBooking() != null && updated.getBooking().getUser() != null) {
            String evTitle = updated.getEvent() != null ? updated.getEvent().getTitle() : "sự kiện";
            loyaltyService.awardPoints(updated.getBooking().getUser(), 20, com.eventhub.enums.LoyaltyActivityType.GATE_CHECKIN_BONUS,
                    "Thưởng check-in tại cổng sự kiện: " + evTitle + " (+20 pts)");
        }

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
                .attendeeEmail(updated.getBooking().getUser().getEmail())
                .attendeePhone(updated.getBooking().getUser().getPhoneNumber())
                .eventId(updated.getEvent().getId())
                .eventTitle(updated.getEvent().getTitle())
                .seatInfo(seatInfo)
                .ticketTypeName(updated.getTicketType() != null ? updated.getTicketType().getName() : "General Admission")
                .checkInTime(updated.getCheckedInAt())
                .staffUsername(staffUsername)
                .bookingCode(updated.getBooking().getBookingCode())
                .status(updated.getStatus().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CheckInResponse verifyTicket(String ticketCode, String staffUsername) {
        log.info("Verifying ticket code: {} by staff: {}", ticketCode, staffUsername);

        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "code", ticketCode));

        String seatInfo = null;
        if (ticket.getEventSeat() != null && ticket.getEventSeat().getSeat() != null) {
            seatInfo = String.format("Row %s - Seat %s (%s)",
                    ticket.getEventSeat().getSeat().getSeatRow(),
                    ticket.getEventSeat().getSeat().getSeatNumber(),
                    ticket.getEventSeat().getSeat().getZone());
        }

        boolean isValid = ticket.getStatus() == TicketStatus.VALID;
        String message = isValid
                ? "Vé hợp lệ, sẵn sàng soát vé"
                : (ticket.getStatus() == TicketStatus.USED
                    ? "Vé đã qua sử dụng (Cảnh báo Anti-Passback)"
                    : "Vé không hợp lệ hoặc đã bị huỷ");

        return CheckInResponse.builder()
                .success(isValid)
                .message(message)
                .ticketCode(ticket.getTicketCode())
                .attendeeName(ticket.getBooking().getUser().getFullName())
                .attendeeEmail(ticket.getBooking().getUser().getEmail())
                .attendeePhone(ticket.getBooking().getUser().getPhoneNumber())
                .eventId(ticket.getEvent().getId())
                .eventTitle(ticket.getEvent().getTitle())
                .seatInfo(seatInfo)
                .ticketTypeName(ticket.getTicketType() != null ? ticket.getTicketType().getName() : "General Admission")
                .checkInTime(ticket.getCheckedInAt())
                .staffUsername(ticket.getCheckedInBy() != null ? ticket.getCheckedInBy().getUsername() : null)
                .bookingCode(ticket.getBooking().getBookingCode())
                .status(ticket.getStatus().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GateStatsResponse getGateStats(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        long totalTickets = ticketRepository.countByEventId(eventId);
        long checkedInCount = ticketRepository.countByEventIdAndStatus(eventId, TicketStatus.USED);
        long remainingCount = Math.max(0, totalTickets - checkedInCount);
        double checkInRate = totalTickets > 0 ? (double) checkedInCount * 100.0 / totalTickets : 0.0;
        checkInRate = Math.round(checkInRate * 10.0) / 10.0;

        List<TicketType> ticketTypes = ticketTypeRepository.findByEventId(eventId);
        List<GateTierStatResponse> tierStats = ticketTypes.stream().map(tt -> {
            long tierTotal = ticketRepository.countByEventIdAndTicketTypeId(eventId, tt.getId());
            long tierCheckedIn = ticketRepository.countByEventIdAndTicketTypeIdAndStatus(eventId, tt.getId(), TicketStatus.USED);
            long tierRemaining = Math.max(0, tierTotal - tierCheckedIn);
            double tierRate = tierTotal > 0 ? (double) tierCheckedIn * 100.0 / tierTotal : 0.0;
            tierRate = Math.round(tierRate * 10.0) / 10.0;

            return GateTierStatResponse.builder()
                    .ticketTypeId(tt.getId())
                    .ticketTypeName(tt.getName())
                    .totalCount(tierTotal)
                    .checkedInCount(tierCheckedIn)
                    .remainingCount(tierRemaining)
                    .checkInRatePercent(tierRate)
                    .build();
        }).collect(Collectors.toList());

        List<Ticket> recentTickets = ticketRepository.findTop20ByEventIdAndStatusOrderByCheckedInAtDesc(eventId, TicketStatus.USED);
        List<CheckInResponse> recentCheckIns = recentTickets.stream().map(t -> {
            String seatInfo = null;
            if (t.getEventSeat() != null && t.getEventSeat().getSeat() != null) {
                seatInfo = String.format("Row %s - Seat %s (%s)",
                        t.getEventSeat().getSeat().getSeatRow(),
                        t.getEventSeat().getSeat().getSeatNumber(),
                        t.getEventSeat().getSeat().getZone());
            }
            return CheckInResponse.builder()
                    .success(true)
                    .message("Checked in")
                    .ticketCode(t.getTicketCode())
                    .attendeeName(t.getBooking().getUser().getFullName())
                    .attendeeEmail(t.getBooking().getUser().getEmail())
                    .attendeePhone(t.getBooking().getUser().getPhoneNumber())
                    .eventId(eventId)
                    .eventTitle(event.getTitle())
                    .seatInfo(seatInfo)
                    .ticketTypeName(t.getTicketType() != null ? t.getTicketType().getName() : "General Admission")
                    .checkInTime(t.getCheckedInAt())
                    .staffUsername(t.getCheckedInBy() != null ? t.getCheckedInBy().getUsername() : null)
                    .bookingCode(t.getBooking().getBookingCode())
                    .status(t.getStatus().name())
                    .build();
        }).collect(Collectors.toList());

        return GateStatsResponse.builder()
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .eventStartTime(event.getStartTime())
                .location(event.getLocation())
                .totalTickets(totalTickets)
                .checkedInCount(checkedInCount)
                .remainingCount(remainingCount)
                .checkInRatePercent(checkInRate)
                .tierStats(tierStats)
                .recentCheckIns(recentCheckIns)
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
