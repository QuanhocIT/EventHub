package com.eventhub.services.impl;

import com.eventhub.dto.request.BookingItemRequest;
import com.eventhub.dto.request.CreateBookingRequest;
import com.eventhub.dto.response.BookingItemResponse;
import com.eventhub.dto.response.BookingResponse;
import com.eventhub.entities.Booking;
import com.eventhub.entities.BookingItem;
import com.eventhub.entities.Event;
import com.eventhub.entities.EventSeat;
import com.eventhub.entities.TicketType;
import com.eventhub.entities.User;
import com.eventhub.enums.BookingStatus;
import com.eventhub.enums.EventSeatStatus;
import com.eventhub.enums.EventStatus;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ForbiddenException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.BookingRepository;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.EventSeatRepository;
import com.eventhub.repositories.TicketTypeRepository;
import com.eventhub.repositories.UserRepository;
import com.eventhub.services.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventSeatRepository eventSeatRepository;
    private final com.eventhub.services.CouponService couponService;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, String currentUsername) {
        log.info("Creating booking for event ID: {} by user: {}", request.getEventId(), currentUsername);

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUsername));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", request.getEventId()));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BadRequestException("Cannot book tickets for an event that is not PUBLISHED");
        }

        if (event.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This event has already ended");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Booking must contain at least one item");
        }

        Booking booking = Booking.builder()
                .bookingCode(generateUniqueBookingCode())
                .user(user)
                .event(event)
                .status(BookingStatus.PENDING_PAYMENT)
                .totalAmount(BigDecimal.ZERO)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        BigDecimal runningTotal = BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();

        for (BookingItemRequest itemReq : request.getItems()) {
            int quantity = itemReq.getQuantity() != null && itemReq.getQuantity() > 0 ? itemReq.getQuantity() : 1;
            BigDecimal unitPrice = BigDecimal.ZERO;
            TicketType ticketType = null;
            EventSeat eventSeat = null;

            if (itemReq.getTicketTypeId() != null) {
                ticketType = ticketTypeRepository.findByIdAndEventIdWithLock(itemReq.getTicketTypeId(), event.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("TicketType", "id", itemReq.getTicketTypeId()));

                if (!Boolean.TRUE.equals(ticketType.getIsActive())) {
                    throw new BadRequestException("Ticket type '" + ticketType.getName() + "' is currently not active");
                }

                if (ticketType.getSaleStartTime() != null && now.isBefore(ticketType.getSaleStartTime())) {
                    throw new BadRequestException("Sale for ticket type '" + ticketType.getName() + "' has not started yet");
                }
                if (ticketType.getSaleEndTime() != null && now.isAfter(ticketType.getSaleEndTime())) {
                    throw new BadRequestException("Sale for ticket type '" + ticketType.getName() + "' has ended");
                }

                if (quantity < ticketType.getMinPerOrder() || quantity > ticketType.getMaxPerOrder()) {
                    throw new BadRequestException(String.format("Quantity for '%s' must be between %d and %d",
                            ticketType.getName(), ticketType.getMinPerOrder(), ticketType.getMaxPerOrder()));
                }

                if (ticketType.getAvailableQuantity() < quantity) {
                    throw new BadRequestException("Not enough tickets available for: " + ticketType.getName());
                }

                ticketType.setAvailableQuantity(ticketType.getAvailableQuantity() - quantity);
                unitPrice = ticketType.getPrice();
            }

            if (itemReq.getEventSeatId() != null) {
                eventSeat = eventSeatRepository.findByIdWithLock(itemReq.getEventSeatId())
                        .orElseThrow(() -> new ResourceNotFoundException("EventSeat", "id", itemReq.getEventSeatId()));

                if (!eventSeat.getEvent().getId().equals(event.getId())) {
                    throw new BadRequestException("EventSeat does not belong to this event");
                }

                if (eventSeat.getStatus() != EventSeatStatus.AVAILABLE) {
                    throw new BadRequestException(String.format("Seat %s%s is not available (Status: %s)",
                            eventSeat.getSeat().getSeatRow(), eventSeat.getSeat().getSeatNumber(), eventSeat.getStatus()));
                }

                eventSeat.setStatus(EventSeatStatus.HELD);
                if (eventSeat.getPrice() != null) {
                    unitPrice = eventSeat.getPrice();
                } else if (unitPrice.compareTo(BigDecimal.ZERO) == 0 && event.getPrice() != null) {
                    unitPrice = event.getPrice();
                }
            }

            if (ticketType == null && eventSeat == null) {
                throw new BadRequestException("Each booking item must specify either ticketTypeId or eventSeatId");
            }

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            runningTotal = runningTotal.add(subtotal);

            BookingItem item = BookingItem.builder()
                    .ticketType(ticketType)
                    .eventSeat(eventSeat)
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            booking.addItem(item);
        }

        booking.setSubtotalAmount(runningTotal);
        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            BigDecimal discount = couponService.applyCouponAndIncrementUsage(request.getCouponCode(), event.getId(), runningTotal);
            booking.setCouponCode(request.getCouponCode().trim().toUpperCase());
            booking.setDiscountAmount(discount);
            BigDecimal finalAmount = runningTotal.subtract(discount);
            booking.setTotalAmount(finalAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalAmount);
        } else {
            booking.setDiscountAmount(BigDecimal.ZERO);
            booking.setTotalAmount(runningTotal);
        }

        Booking saved = bookingRepository.save(booking);

        log.info("Booking created successfully with code: {} and total: {}", saved.getBookingCode(), saved.getTotalAmount());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getMyBookings(String currentUsername, Pageable pageable) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUsername));

        return bookingRepository.findByUserId(user.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByCode(String bookingCode, String currentUsername, boolean isAdminOrOrganizer) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "code", bookingCode));

        boolean isCustomerOwner = booking.getUser().getUsername().equals(currentUsername);
        if (!isCustomerOwner && !isAdminOrOrganizer) {
            throw new ForbiddenException("You do not have permission to view this booking");
        }

        return mapToResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(String bookingCode, String currentUsername, boolean isAdmin) {
        log.info("Cancelling booking: {} by user: {}", bookingCode, currentUsername);

        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "code", bookingCode));

        boolean isCustomerOwner = booking.getUser().getUsername().equals(currentUsername);
        if (!isCustomerOwner && !isAdmin) {
            throw new ForbiddenException("You do not have permission to cancel this booking");
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Only bookings in PENDING_PAYMENT status can be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        // Release inventory back
        for (BookingItem item : booking.getItems()) {
            if (item.getTicketType() != null) {
                TicketType ticketType = item.getTicketType();
                ticketType.setAvailableQuantity(ticketType.getAvailableQuantity() + item.getQuantity());
            }
            if (item.getEventSeat() != null) {
                EventSeat eventSeat = item.getEventSeat();
                eventSeat.setStatus(EventSeatStatus.AVAILABLE);
            }
        }

        // Release coupon if used
        if (booking.getCouponCode() != null && !booking.getCouponCode().isBlank()) {
            couponService.releaseCouponUsage(booking.getCouponCode());
        }

        log.info("Booking {} cancelled and inventory released", bookingCode);
        return mapToResponse(booking);
    }

    private String generateUniqueBookingCode() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        for (int i = 0; i < 5; i++) {
            int randomNum = 1000 + RANDOM.nextInt(9000);
            String code = String.format("EVH-%s-%04d", datePrefix, randomNum);
            if (bookingRepository.findByBookingCode(code).isEmpty()) {
                return code;
            }
        }
        return String.format("EVH-%s-%d", datePrefix, System.currentTimeMillis() % 10000);
    }

    private BookingResponse mapToResponse(Booking booking) {
        List<BookingItemResponse> itemResponses = booking.getItems().stream().map(item -> {
            String seatInfo = null;
            if (item.getEventSeat() != null && item.getEventSeat().getSeat() != null) {
                seatInfo = String.format("Row %s - Seat %s (%s)",
                        item.getEventSeat().getSeat().getSeatRow(),
                        item.getEventSeat().getSeat().getSeatNumber(),
                        item.getEventSeat().getSeat().getZone());
            }

            return BookingItemResponse.builder()
                    .id(item.getId())
                    .ticketTypeId(item.getTicketType() != null ? item.getTicketType().getId() : null)
                    .ticketTypeName(item.getTicketType() != null ? item.getTicketType().getName() : null)
                    .eventSeatId(item.getEventSeat() != null ? item.getEventSeat().getId() : null)
                    .seatInfo(seatInfo)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .subtotal(item.getSubtotal())
                    .build();
        }).toList();

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .eventId(booking.getEvent().getId())
                .eventTitle(booking.getEvent().getTitle())
                .username(booking.getUser().getUsername())
                .status(booking.getStatus())
                .subtotalAmount(booking.getSubtotalAmount() != null ? booking.getSubtotalAmount() : booking.getTotalAmount())
                .discountAmount(booking.getDiscountAmount() != null ? booking.getDiscountAmount() : BigDecimal.ZERO)
                .couponCode(booking.getCouponCode())
                .totalAmount(booking.getTotalAmount())
                .expiresAt(booking.getExpiresAt())
                .items(itemResponses)
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
