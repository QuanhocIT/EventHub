package com.eventhub.services;

import com.eventhub.dto.response.CheckInResponse;
import com.eventhub.dto.response.TicketResponse;
import com.eventhub.entities.Booking;
import com.eventhub.entities.BookingItem;
import com.eventhub.entities.Event;
import com.eventhub.entities.EventSeat;
import com.eventhub.entities.Seat;
import com.eventhub.entities.Ticket;
import com.eventhub.entities.TicketType;
import com.eventhub.entities.User;
import com.eventhub.enums.SeatZone;
import com.eventhub.enums.TicketStatus;
import com.eventhub.enums.UserRole;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ForbiddenException;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.TicketRepository;
import com.eventhub.repositories.TicketTypeRepository;
import com.eventhub.repositories.UserRepository;
import com.eventhub.services.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private QrCodeService qrCodeService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private User attendee;
    private User staff;
    private Event event;
    private Booking booking;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        attendee = User.builder()
                .id(1L)
                .username("alice")
                .fullName("Alice Wonderland")
                .role(UserRole.ROLE_CUSTOMER)
                .build();

        staff = User.builder()
                .id(2L)
                .username("bob_staff")
                .fullName("Bob Security")
                .role(UserRole.ROLE_STAFF)
                .build();

        event = Event.builder()
                .id(10L)
                .title("Symphony Concert 2026")
                .build();

        booking = Booking.builder()
                .id(100L)
                .bookingCode("EVH-20261021-0001")
                .user(attendee)
                .event(event)
                .build();

        Seat seat = Seat.builder().seatRow("B").seatNumber(7).zone(SeatZone.VIP).build();
        EventSeat eventSeat = EventSeat.builder().id(55L).seat(seat).build();
        TicketType ticketType = TicketType.builder().id(22L).name("VIP Pass").build();

        ticket = Ticket.builder()
                .id(1L)
                .ticketCode("TK-20261021-AAAA1111")
                .booking(booking)
                .event(event)
                .ticketType(ticketType)
                .eventSeat(eventSeat)
                .status(TicketStatus.VALID)
                .qrCodeData("eventhub://tickets/TK-20261021-AAAA1111?event=10")
                .build();
    }

    @Test
    @DisplayName("Should generate tickets for confirmed booking items")
    void generateTicketsForBooking_Success() {
        BookingItem item1 = BookingItem.builder().quantity(2).build();
        BookingItem item2 = BookingItem.builder().quantity(1).build();
        booking.addItem(item1);
        booking.addItem(item2);

        when(ticketRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<Ticket> generated = ticketService.generateTicketsForBooking(booking);

        assertThat(generated).hasSize(3); // 2 + 1
        assertThat(generated.get(0).getStatus()).isEqualTo(TicketStatus.VALID);
        assertThat(generated.get(0).getTicketCode()).startsWith("TK-");
        verify(ticketRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should return user tickets with QR codes")
    void getMyTickets_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(attendee));
        when(ticketRepository.findByBookingUserId(1L, pageable)).thenReturn(new PageImpl<>(List.of(ticket)));
        when(qrCodeService.generateQrCodeBase64(anyString(), anyInt(), anyInt())).thenReturn("data:image/png;base64,sample");

        Page<TicketResponse> result = ticketService.getMyTickets("alice", pageable);

        assertThat(result.getContent()).hasSize(1);
        TicketResponse response = result.getContent().get(0);
        assertThat(response.getTicketCode()).isEqualTo("TK-20261021-AAAA1111");
        assertThat(response.getAttendeeName()).isEqualTo("Alice Wonderland");
        assertThat(response.getSeatInfo()).contains("Row B - Seat 7 (VIP)");
        assertThat(response.getQrCodeBase64()).isEqualTo("data:image/png;base64,sample");
    }

    @Test
    @DisplayName("Should throw ForbiddenException when user views another person's ticket")
    void getTicketByCode_Forbidden() {
        when(ticketRepository.findByTicketCode("TK-20261021-AAAA1111")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.getTicketByCode("TK-20261021-AAAA1111", "hacker", false))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("permission");
    }

    @Test
    @DisplayName("Should check in valid ticket successfully and record staff & timestamp")
    void checkInTicket_Success() {
        when(userRepository.findByUsername("bob_staff")).thenReturn(Optional.of(staff));
        when(ticketRepository.findByTicketCode("TK-20261021-AAAA1111")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        CheckInResponse response = ticketService.checkInTicket("TK-20261021-AAAA1111", "bob_staff");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStaffUsername()).isEqualTo("bob_staff");
        assertThat(response.getAttendeeName()).isEqualTo("Alice Wonderland");
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.USED);
        assertThat(ticket.getCheckedInAt()).isNotNull();
        assertThat(ticket.getCheckedInBy()).isEqualTo(staff);

        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("Should throw BadRequestException when ticket was already checked in (anti-passback)")
    void checkInTicket_Duplicate_ThrowsBadRequestException() {
        ticket.setStatus(TicketStatus.USED);
        ticket.setCheckedInAt(LocalDateTime.now().minusHours(1));

        when(userRepository.findByUsername("bob_staff")).thenReturn(Optional.of(staff));
        when(ticketRepository.findByTicketCode("TK-20261021-AAAA1111")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.checkInTicket("TK-20261021-AAAA1111", "bob_staff"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already used for check-in");
    }

    @Test
    @DisplayName("Should verify valid ticket without modifying status")
    void verifyTicket_Valid() {
        when(ticketRepository.findByTicketCode("TK-20261021-AAAA1111")).thenReturn(Optional.of(ticket));

        CheckInResponse response = ticketService.verifyTicket("TK-20261021-AAAA1111", "bob_staff");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatus()).isEqualTo("VALID");
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.VALID);
    }

    @Test
    @DisplayName("Should return gate stats with tier breakdowns and recent check-ins")
    void getGateStats_Success() {
        TicketType ticketType = TicketType.builder().id(22L).name("VIP Pass").build();
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(ticketRepository.countByEventId(10L)).thenReturn(100L);
        when(ticketRepository.countByEventIdAndStatus(10L, TicketStatus.USED)).thenReturn(40L);
        when(ticketTypeRepository.findByEventId(10L)).thenReturn(List.of(ticketType));
        when(ticketRepository.countByEventIdAndTicketTypeId(10L, 22L)).thenReturn(50L);
        when(ticketRepository.countByEventIdAndTicketTypeIdAndStatus(10L, 22L, TicketStatus.USED)).thenReturn(25L);
        when(ticketRepository.findTop20ByEventIdAndStatusOrderByCheckedInAtDesc(10L, TicketStatus.USED))
                .thenReturn(List.of(ticket));

        com.eventhub.dto.response.GateStatsResponse stats = ticketService.getGateStats(10L);

        assertThat(stats.getEventId()).isEqualTo(10L);
        assertThat(stats.getTotalTickets()).isEqualTo(100L);
        assertThat(stats.getCheckedInCount()).isEqualTo(40L);
        assertThat(stats.getRemainingCount()).isEqualTo(60L);
        assertThat(stats.getCheckInRatePercent()).isEqualTo(40.0);
        assertThat(stats.getTierStats()).hasSize(1);
        assertThat(stats.getTierStats().get(0).getTicketTypeName()).isEqualTo("VIP Pass");
        assertThat(stats.getTierStats().get(0).getCheckInRatePercent()).isEqualTo(50.0);
        assertThat(stats.getRecentCheckIns()).hasSize(1);
    }
}
