package com.eventhub.services;

import com.eventhub.dto.request.CreatePaymentRequest;
import com.eventhub.dto.request.ProcessPaymentRequest;
import com.eventhub.dto.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request, String currentUsername);

    PaymentResponse processPayment(ProcessPaymentRequest request, String currentUsername, boolean isAdmin);

    PaymentResponse getPaymentByBookingCode(String bookingCode, String currentUsername, boolean isAdminOrOrganizer);

    PaymentResponse getPaymentStatusByTransactionCode(String transactionCode, String currentUsername, boolean isAdmin);

    PaymentResponse simulateInstantPaymentNotification(String transactionCode);
}
