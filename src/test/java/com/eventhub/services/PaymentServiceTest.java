package com.eventhub.services;

import com.eventhub.dto.request.CreatePaymentRequest;
import com.eventhub.dto.request.ProcessPaymentRequest;
import com.eventhub.dto.response.PaymentResponse;
import com.eventhub.entities.Booking;
import com.eventhub.entities.BookingItem;
import com.eventhub.entities.Event;
import com.eventhub.entities.EventSeat;
import com.eventhub.entities.Payment;
import com.eventhub.entities.Seat;
import com.eventhub.entities.User;
import com.eventhub.enums.BookingStatus;
import com.eventhub.enums.EventSeatStatus;
import com.eventhub.enums.PaymentProvider;
import com.eventhub.enums.PaymentStatus;
import com.eventhub.enums.SeatZone;
import com.eventhub.enums.UserRole;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.repositories.BookingRepository;
import com.eventhub.repositories.PaymentRepository;
import com.eventhub.services.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private com.eventhub.services.TicketService ticketService;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User customer;
    private Event event;
    private Booking pendingBooking;
    private EventSeat eventSeat;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .id(1L)
                .username("john_doe")
                .role(UserRole.ROLE_CUSTOMER)
                .build();

        event = Event.builder()
                .id(10L)
                .title("Tech Summit 2026")
                .build();

        Seat seat = Seat.builder().seatRow("A").seatNumber(1).zone(SeatZone.VIP).build();
        eventSeat = EventSeat.builder()
                .id(50L)
                .seat(seat)
                .status(EventSeatStatus.HELD)
                .build();

        pendingBooking = Booking.builder()
                .id(100L)
                .bookingCode("EVH-20261021-0001")
                .user(customer)
                .event(event)
                .status(BookingStatus.PENDING_PAYMENT)
                .totalAmount(new BigDecimal("500000"))
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        BookingItem item = BookingItem.builder()
                .booking(pendingBooking)
                .eventSeat(eventSeat)
                .quantity(1)
                .unitPrice(new BigDecimal("500000"))
                .subtotal(new BigDecimal("500000"))
                .build();

        pendingBooking.addItem(item);
    }

    @Test
    @DisplayName("Should create payment successfully for pending booking")
    void createPayment_Success() {
        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .bookingCode("EVH-20261021-0001")
                .provider(PaymentProvider.MOCK_SANDBOX)
                .build();

        when(bookingRepository.findByBookingCode("EVH-20261021-0001")).thenReturn(Optional.of(pendingBooking));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        PaymentResponse response = paymentService.createPayment(request, "john_doe");

        assertThat(response).isNotNull();
        assertThat(response.getBookingCode()).isEqualTo("EVH-20261021-0001");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(response.getAmount()).isEqualByComparingTo("500000");
        assertThat(response.getPaymentUrl()).contains("sandbox.eventhub.com/pay/");

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should return existing payment when idempotencyKey matches already successful payment")
    void createPayment_Idempotent_ReturnsExistingSuccessful() {
        Payment existing = Payment.builder()
                .id(2L)
                .booking(pendingBooking)
                .transactionCode("TXN-EXISTING")
                .idempotencyKey("KEY-123")
                .amount(new BigDecimal("500000"))
                .status(PaymentStatus.SUCCESS)
                .build();

        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .bookingCode("EVH-20261021-0001")
                .idempotencyKey("KEY-123")
                .build();

        when(bookingRepository.findByBookingCode("EVH-20261021-0001")).thenReturn(Optional.of(pendingBooking));
        when(paymentRepository.findByIdempotencyKey("KEY-123")).thenReturn(Optional.of(existing));

        PaymentResponse response = paymentService.createPayment(request, "john_doe");

        assertThat(response.getTransactionCode()).isEqualTo("TXN-EXISTING");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when booking has expired")
    void createPayment_ExpiredBooking_ThrowsBadRequestException() {
        pendingBooking.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .bookingCode("EVH-20261021-0001")
                .build();

        when(bookingRepository.findByBookingCode("EVH-20261021-0001")).thenReturn(Optional.of(pendingBooking));

        assertThatThrownBy(() -> paymentService.createPayment(request, "john_doe"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("Should process successful payment, confirm booking and mark seats as SOLD")
    void processPayment_Success_ConfirmsBookingAndMarksSeatsSold() {
        Payment payment = Payment.builder()
                .id(1L)
                .booking(pendingBooking)
                .transactionCode("TXN-20261021-ABCD")
                .amount(new BigDecimal("500000"))
                .status(PaymentStatus.PENDING)
                .build();

        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .transactionCode("TXN-20261021-ABCD")
                .success(true)
                .build();

        when(paymentRepository.findByTransactionCode("TXN-20261021-ABCD")).thenReturn(Optional.of(payment));
        when(redisTemplateProvider.getIfAvailable()).thenReturn(redisTemplate);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.processPayment(request, "john_doe", false);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(pendingBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(eventSeat.getStatus()).isEqualTo(EventSeatStatus.SOLD);
        verify(redisTemplate).delete("seat_hold:10:50");
        verify(ticketService).generateTicketsForBooking(pendingBooking);
        verify(eventPublisher).publishEvent(any(com.eventhub.events.BookingConfirmedEvent.class));
    }

    @Test
    @DisplayName("Should be idempotent when processing an already successful payment")
    void processPayment_Idempotent_WhenAlreadySuccessful() {
        Payment payment = Payment.builder()
                .id(1L)
                .booking(pendingBooking)
                .transactionCode("TXN-ALREADY-SUCCESS")
                .status(PaymentStatus.SUCCESS)
                .amount(new BigDecimal("500000"))
                .build();

        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .transactionCode("TXN-ALREADY-SUCCESS")
                .success(true)
                .build();

        when(paymentRepository.findByTransactionCode("TXN-ALREADY-SUCCESS")).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.processPayment(request, "john_doe", false);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should set payment status to FAILED when transaction fails")
    void processPayment_Failure() {
        Payment payment = Payment.builder()
                .id(1L)
                .booking(pendingBooking)
                .transactionCode("TXN-FAIL")
                .amount(new BigDecimal("500000"))
                .status(PaymentStatus.PENDING)
                .build();

        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .transactionCode("TXN-FAIL")
                .success(false)
                .build();

        when(paymentRepository.findByTransactionCode("TXN-FAIL")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.processPayment(request, "john_doe", false);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(pendingBooking.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT); // untouched
        assertThat(eventSeat.getStatus()).isEqualTo(EventSeatStatus.HELD); // still held
    }

    @Test
    @DisplayName("Should return payment details for booking")
    void getPaymentByBookingCode_Success() {
        Payment payment = Payment.builder()
                .id(1L)
                .booking(pendingBooking)
                .transactionCode("TXN-VIEW")
                .amount(new BigDecimal("500000"))
                .status(PaymentStatus.SUCCESS)
                .build();

        when(bookingRepository.findByBookingCode("EVH-20261021-0001")).thenReturn(Optional.of(pendingBooking));
        when(paymentRepository.findFirstByBookingIdOrderByCreatedAtDesc(100L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentByBookingCode("EVH-20261021-0001", "john_doe", false);

        assertThat(response).isNotNull();
        assertThat(response.getTransactionCode()).isEqualTo("TXN-VIEW");
    }

    @Test
    @DisplayName("Should simulate IPN webhook and confirm payment automatically")
    void simulateInstantPaymentNotification_Success() {
        Payment payment = Payment.builder()
                .id(1L)
                .booking(pendingBooking)
                .transactionCode("TXN-IPN-123")
                .amount(new BigDecimal("500000"))
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findByTransactionCode("TXN-IPN-123")).thenReturn(Optional.of(payment));
        when(redisTemplateProvider.getIfAvailable()).thenReturn(redisTemplate);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.simulateInstantPaymentNotification("TXN-IPN-123");

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(pendingBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(eventSeat.getStatus()).isEqualTo(EventSeatStatus.SOLD);
        verify(ticketService).generateTicketsForBooking(pendingBooking);
        verify(eventPublisher).publishEvent(any(com.eventhub.events.BookingConfirmedEvent.class));
    }

    @Test
    @DisplayName("Should poll payment status by transaction code")
    void getPaymentStatusByTransactionCode_Success() {
        Payment payment = Payment.builder()
                .id(1L)
                .booking(pendingBooking)
                .transactionCode("TXN-POLL-123")
                .amount(new BigDecimal("500000"))
                .status(PaymentStatus.SUCCESS)
                .build();

        when(paymentRepository.findByTransactionCode("TXN-POLL-123")).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentStatusByTransactionCode("TXN-POLL-123", "john_doe", false);

        assertThat(response).isNotNull();
        assertThat(response.getTransactionCode()).isEqualTo("TXN-POLL-123");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }
}
