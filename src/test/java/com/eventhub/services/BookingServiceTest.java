package com.eventhub.services;

import com.eventhub.dto.request.BookingItemRequest;
import com.eventhub.dto.request.CreateBookingRequest;
import com.eventhub.dto.response.BookingResponse;
import com.eventhub.entities.Booking;
import com.eventhub.entities.BookingItem;
import com.eventhub.entities.Event;
import com.eventhub.entities.EventSeat;
import com.eventhub.entities.Seat;
import com.eventhub.entities.TicketType;
import com.eventhub.entities.User;
import com.eventhub.enums.BookingStatus;
import com.eventhub.enums.EventSeatStatus;
import com.eventhub.enums.EventStatus;
import com.eventhub.enums.SeatZone;
import com.eventhub.enums.UserRole;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ForbiddenException;
import com.eventhub.repositories.BookingRepository;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.EventSeatRepository;
import com.eventhub.repositories.TicketTypeRepository;
import com.eventhub.repositories.UserRepository;
import com.eventhub.services.impl.BookingServiceImpl;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private EventSeatRepository eventSeatRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User customer;
    private Event publishedEvent;
    private TicketType ticketType;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .id(1L)
                .username("john_customer")
                .role(UserRole.ROLE_CUSTOMER)
                .build();

        publishedEvent = Event.builder()
                .id(10L)
                .title("Rock Concert 2026")
                .status(EventStatus.PUBLISHED)
                .endTime(LocalDateTime.now().plusDays(10))
                .price(new BigDecimal("500000"))
                .build();

        ticketType = TicketType.builder()
                .id(20L)
                .event(publishedEvent)
                .name("VIP Zone")
                .price(new BigDecimal("750000"))
                .totalQuantity(50)
                .availableQuantity(50)
                .minPerOrder(1)
                .maxPerOrder(5)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should create booking successfully for ticket type with stock deduction")
    void createBooking_Success_WithTicketType() {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .eventId(10L)
                .items(List.of(
                        BookingItemRequest.builder()
                                .ticketTypeId(20L)
                                .quantity(2)
                                .build()
                ))
                .build();

        when(userRepository.findByUsername("john_customer")).thenReturn(Optional.of(customer));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(publishedEvent));
        when(ticketTypeRepository.findByIdAndEventId(20L, 10L)).thenReturn(Optional.of(ticketType));
        when(bookingRepository.findByBookingCode(any())).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(100L);
            return b;
        });

        BookingResponse response = bookingService.createBooking(request, "john_customer");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("1500000"); // 750000 * 2
        assertThat(ticketType.getAvailableQuantity()).isEqualTo(48); // 50 - 2

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should create booking successfully with seat hold")
    void createBooking_Success_WithEventSeat() {
        Seat physicalSeat = Seat.builder()
                .seatRow("A")
                .seatNumber(1)
                .zone(SeatZone.VIP)
                .build();

        EventSeat eventSeat = EventSeat.builder()
                .id(30L)
                .event(publishedEvent)
                .seat(physicalSeat)
                .status(EventSeatStatus.AVAILABLE)
                .price(new BigDecimal("1000000"))
                .build();

        CreateBookingRequest request = CreateBookingRequest.builder()
                .eventId(10L)
                .items(List.of(
                        BookingItemRequest.builder()
                                .eventSeatId(30L)
                                .quantity(1)
                                .build()
                ))
                .build();

        when(userRepository.findByUsername("john_customer")).thenReturn(Optional.of(customer));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(publishedEvent));
        when(eventSeatRepository.findById(30L)).thenReturn(Optional.of(eventSeat));
        when(bookingRepository.findByBookingCode(any())).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(101L);
            return b;
        });

        BookingResponse response = bookingService.createBooking(request, "john_customer");

        assertThat(response).isNotNull();
        assertThat(response.getTotalAmount()).isEqualByComparingTo("1000000");
        assertThat(eventSeat.getStatus()).isEqualTo(EventSeatStatus.HELD);
    }

    @Test
    @DisplayName("Should throw BadRequestException when event is DRAFT (not published)")
    void createBooking_UnpublishedEvent_ThrowsBadRequestException() {
        publishedEvent.setStatus(EventStatus.DRAFT);
        CreateBookingRequest request = CreateBookingRequest.builder()
                .eventId(10L)
                .items(List.of(BookingItemRequest.builder().ticketTypeId(20L).quantity(1).build()))
                .build();

        when(userRepository.findByUsername("john_customer")).thenReturn(Optional.of(customer));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(publishedEvent));

        assertThatThrownBy(() -> bookingService.createBooking(request, "john_customer"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("PUBLISHED");
    }

    @Test
    @DisplayName("Should throw BadRequestException when available quantity is insufficient")
    void createBooking_InsufficientStock_ThrowsBadRequestException() {
        ticketType.setAvailableQuantity(1);

        CreateBookingRequest request = CreateBookingRequest.builder()
                .eventId(10L)
                .items(List.of(BookingItemRequest.builder().ticketTypeId(20L).quantity(2).build()))
                .build();

        when(userRepository.findByUsername("john_customer")).thenReturn(Optional.of(customer));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(publishedEvent));
        when(ticketTypeRepository.findByIdAndEventId(20L, 10L)).thenReturn(Optional.of(ticketType));

        assertThatThrownBy(() -> bookingService.createBooking(request, "john_customer"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Not enough tickets available");
    }

    @Test
    @DisplayName("Should cancel booking and restore stock and seat status")
    void cancelBooking_Success_ReleasesInventory() {
        Seat physicalSeat = Seat.builder().seatRow("B").seatNumber(5).zone(SeatZone.STANDARD).build();
        EventSeat eventSeat = EventSeat.builder()
                .id(35L)
                .seat(physicalSeat)
                .status(EventSeatStatus.HELD)
                .build();

        ticketType.setAvailableQuantity(48);

        Booking booking = Booking.builder()
                .id(200L)
                .bookingCode("EVH-20261021-0001")
                .user(customer)
                .event(publishedEvent)
                .status(BookingStatus.PENDING_PAYMENT)
                .totalAmount(new BigDecimal("1500000"))
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        BookingItem item1 = BookingItem.builder()
                .booking(booking)
                .ticketType(ticketType)
                .quantity(2)
                .unitPrice(new BigDecimal("750000"))
                .subtotal(new BigDecimal("1500000"))
                .build();

        BookingItem item2 = BookingItem.builder()
                .booking(booking)
                .eventSeat(eventSeat)
                .quantity(1)
                .unitPrice(new BigDecimal("500000"))
                .subtotal(new BigDecimal("500000"))
                .build();

        booking.addItem(item1);
        booking.addItem(item2);

        when(bookingRepository.findByBookingCode("EVH-20261021-0001")).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.cancelBooking("EVH-20261021-0001", "john_customer", false);

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(ticketType.getAvailableQuantity()).isEqualTo(50); // 48 + 2 restored
        assertThat(eventSeat.getStatus()).isEqualTo(EventSeatStatus.AVAILABLE); // released
    }

    @Test
    @DisplayName("Should throw ForbiddenException when user cancels someone else's booking")
    void cancelBooking_Forbidden() {
        Booking booking = Booking.builder()
                .bookingCode("EVH-20261021-0002")
                .user(customer)
                .status(BookingStatus.PENDING_PAYMENT)
                .build();

        when(bookingRepository.findByBookingCode("EVH-20261021-0002")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking("EVH-20261021-0002", "other_user", false))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("permission");
    }

    @Test
    @DisplayName("Should return user booking history")
    void getMyBookings_Success() {
        Booking booking = Booking.builder()
                .id(1L)
                .bookingCode("EVH-20261021-0003")
                .user(customer)
                .event(publishedEvent)
                .status(BookingStatus.CONFIRMED)
                .totalAmount(new BigDecimal("500000"))
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findByUsername("john_customer")).thenReturn(Optional.of(customer));
        when(bookingRepository.findByUserId(1L, pageable)).thenReturn(new PageImpl<>(List.of(booking)));

        Page<BookingResponse> result = bookingService.getMyBookings("john_customer", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBookingCode()).isEqualTo("EVH-20261021-0003");
    }
}
