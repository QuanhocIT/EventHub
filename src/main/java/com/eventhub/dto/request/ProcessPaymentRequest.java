package com.eventhub.dto.request;

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
public class ProcessPaymentRequest {

    @NotBlank(message = "Transaction code is required")
    private String transactionCode;

    @Builder.Default
    private Boolean success = true;

    private String idempotencyKey;
}
