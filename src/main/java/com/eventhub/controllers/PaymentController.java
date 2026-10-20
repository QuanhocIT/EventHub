package com.eventhub.controllers;

import com.eventhub.common.ApiResponse;
import com.eventhub.dto.request.CreatePaymentRequest;
import com.eventhub.dto.request.ProcessPaymentRequest;
import com.eventhub.dto.response.PaymentResponse;
import com.eventhub.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Tag(name = "Payments", description = "Payment management and sandbox processing APIs")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Initiate payment for a pending booking")
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String headerIdempotencyKey,
            Principal principal
    ) {
        if (request.getIdempotencyKey() == null && headerIdempotencyKey != null) {
            request.setIdempotencyKey(headerIdempotencyKey);
        }

        PaymentResponse response = paymentService.createPayment(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment initiated successfully", response));
    }

    @Operation(summary = "Process sandbox payment (Simulate payment gateway callback)")
    @PostMapping("/process-sandbox")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String headerIdempotencyKey,
            Principal principal,
            Authentication authentication
    ) {
        if (request.getIdempotencyKey() == null && headerIdempotencyKey != null) {
            request.setIdempotencyKey(headerIdempotencyKey);
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        PaymentResponse response = paymentService.processPayment(request, principal.getName(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", response));
    }

    @Operation(summary = "Get payment details for a booking")
    @GetMapping("/booking/{bookingCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByBookingCode(
            @PathVariable String bookingCode,
            Principal principal,
            Authentication authentication
    ) {
        boolean isAdminOrOrganizer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ORGANIZER"));

        PaymentResponse response = paymentService.getPaymentByBookingCode(bookingCode, principal.getName(), isAdminOrOrganizer);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
