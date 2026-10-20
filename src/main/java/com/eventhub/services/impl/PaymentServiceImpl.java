package com.eventhub.services.impl;

import com.eventhub.dto.request.CreatePaymentRequest;
import com.eventhub.dto.request.ProcessPaymentRequest;
import com.eventhub.dto.response.PaymentResponse;
import com.eventhub.entities.Booking;
import com.eventhub.entities.BookingItem;
import com.eventhub.entities.EventSeat;
import com.eventhub.entities.Payment;
import com.eventhub.enums.BookingStatus;
import com.eventhub.enums.EventSeatStatus;
import com.eventhub.enums.PaymentProvider;
import com.eventhub.enums.PaymentStatus;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ForbiddenException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.BookingRepository;
import com.eventhub.repositories.PaymentRepository;
import com.eventhub.events.BookingConfirmedEvent;
import com.eventhub.services.PaymentService;
import com.eventhub.services.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider;
    private final TicketService ticketService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request, String currentUsername) {
        log.info("Initiating payment for booking: {} by user: {}", request.getBookingCode(), currentUsername);

        Booking booking = bookingRepository.findByBookingCode(request.getBookingCode())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "code", request.getBookingCode()));

        boolean isOwner = booking.getUser().getUsername().equals(currentUsername);
        if (!isOwner) {
            throw new ForbiddenException("You do not have permission to pay for this booking");
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new BadRequestException("Booking has already been paid and confirmed");
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Booking is not in PENDING_PAYMENT status");
        }

        if (booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Booking has expired. Please create a new booking.");
        }

        // Idempotency check on key
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingPayment.isPresent()) {
                Payment payment = existingPayment.get();
                if (payment.getStatus() == PaymentStatus.SUCCESS) {
                    log.info("Idempotency match: returning existing successful payment for key: {}", request.getIdempotencyKey());
                    return mapToResponse(payment);
                }
            }
        }

        String transactionCode = generateTransactionCode();
        PaymentProvider provider = request.getProvider() != null ? request.getProvider() : PaymentProvider.MOCK_SANDBOX;
        String paymentUrl = String.format("https://sandbox.eventhub.com/pay/%s", transactionCode);

        Payment payment = Payment.builder()
                .booking(booking)
                .transactionCode(transactionCode)
                .idempotencyKey(request.getIdempotencyKey())
                .provider(provider)
                .amount(booking.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .paymentUrl(paymentUrl)
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment created with transaction code: {} for amount: {}", saved.getTransactionCode(), saved.getAmount());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request, String currentUsername, boolean isAdmin) {
        log.info("Processing payment for transaction: {} by user: {} (success: {})",
                request.getTransactionCode(), currentUsername, request.getSuccess());

        Payment payment = paymentRepository.findByTransactionCode(request.getTransactionCode())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionCode", request.getTransactionCode()));

        boolean isCustomerOwner = payment.getBooking().getUser().getUsername().equals(currentUsername);
        if (!isCustomerOwner && !isAdmin) {
            throw new ForbiddenException("You do not have permission to process this payment");
        }

        // Idempotency check: Already processed successfully
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.warn("Idempotency check: Payment {} is already SUCCESS. Returning current state.", request.getTransactionCode());
            return mapToResponse(payment);
        }

        if (Boolean.TRUE.equals(request.getSuccess())) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaymentTime(LocalDateTime.now());

            Booking booking = payment.getBooking();
            booking.setStatus(BookingStatus.CONFIRMED);

            // Mark reserved seats as SOLD and release Redis holds
            RedisTemplate<String, Object> redisTemplate = redisTemplateProvider.getIfAvailable();
            for (BookingItem item : booking.getItems()) {
                if (item.getEventSeat() != null) {
                    EventSeat eventSeat = item.getEventSeat();
                    eventSeat.setStatus(EventSeatStatus.SOLD);

                    if (redisTemplate != null) {
                        String redisKey = String.format("seat_hold:%d:%d",
                                booking.getEvent().getId(), eventSeat.getId());
                        redisTemplate.delete(redisKey);
                    }
                }
            }

            // Issue e-tickets
            ticketService.generateTicketsForBooking(booking);

            // Broadcast domain event to trigger asynchronous email notifications
            eventPublisher.publishEvent(BookingConfirmedEvent.builder()
                    .bookingId(booking.getId())
                    .bookingCode(booking.getBookingCode())
                    .attendeeEmail(booking.getUser().getEmail())
                    .attendeeName(booking.getUser().getFullName())
                    .eventTitle(booking.getEvent().getTitle())
                    .totalAmount(booking.getTotalAmount())
                    .build());

            log.info("Payment {} SUCCESS. Booking {} CONFIRMED, seats marked SOLD, and e-tickets issued.",
                    payment.getTransactionCode(), booking.getBookingCode());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage("Payment was rejected or cancelled");
            log.info("Payment {} FAILED.", payment.getTransactionCode());
        }

        Payment updated = paymentRepository.save(payment);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByBookingCode(String bookingCode, String currentUsername, boolean isAdminOrOrganizer) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "code", bookingCode));

        boolean isOwner = booking.getUser().getUsername().equals(currentUsername);
        if (!isOwner && !isAdminOrOrganizer) {
            throw new ForbiddenException("You do not have permission to view payment for this booking");
        }

        Payment payment = paymentRepository.findFirstByBookingIdOrderByCreatedAtDesc(booking.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment for booking", "code", bookingCode));

        return mapToResponse(payment);
    }

    private String generateTransactionCode() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return String.format("TXN-%s-%s", datePrefix, randomSuffix);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .transactionCode(payment.getTransactionCode())
                .bookingCode(payment.getBooking().getBookingCode())
                .eventId(payment.getBooking().getEvent().getId())
                .eventTitle(payment.getBooking().getEvent().getTitle())
                .amount(payment.getAmount())
                .provider(payment.getProvider())
                .status(payment.getStatus())
                .paymentUrl(payment.getPaymentUrl())
                .paymentTime(payment.getPaymentTime())
                .errorMessage(payment.getErrorMessage())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
