package com.eventhub.services;

import java.util.concurrent.CompletableFuture;

public interface EmailService {

    CompletableFuture<Boolean> sendBookingConfirmation(String toEmail, String recipientName, String bookingCode, String eventTitle, String totalAmount);

    CompletableFuture<Boolean> sendEventCancelledNotification(String toEmail, String recipientName, String eventTitle, String reason);
}
