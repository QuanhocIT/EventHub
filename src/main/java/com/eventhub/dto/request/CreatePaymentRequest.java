package com.eventhub.dto.request;

import com.eventhub.enums.PaymentProvider;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {

    @NotBlank(message = "Booking code is required")
    private String bookingCode;

    @Builder.Default
    private PaymentProvider provider = PaymentProvider.MOCK_SANDBOX;

    private String idempotencyKey;
}
