package com.eventhub.services;

import com.eventhub.services.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

class EmailServiceTest {

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl();
    }

    @Test
    @DisplayName("Should send booking confirmation email asynchronously and return true")
    void sendBookingConfirmation_Success() throws ExecutionException, InterruptedException {
        CompletableFuture<Boolean> future = emailService.sendBookingConfirmation(
                "customer@example.com",
                "Alice",
                "EVH-20261021-0001",
                "Rock Festival 2026",
                "1,500,000"
        );

        Boolean result = future.get();
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should send event cancellation notice and return true")
    void sendEventCancelledNotification_Success() throws ExecutionException, InterruptedException {
        CompletableFuture<Boolean> future = emailService.sendEventCancelledNotification(
                "customer@example.com",
                "Alice",
                "Rock Festival 2026",
                "Inclement weather conditions"
        );

        Boolean result = future.get();
        assertThat(result).isTrue();
    }
}
