package com.eventhub.dto.response;

import com.eventhub.enums.PaymentProvider;
import com.eventhub.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long id;
    private String transactionCode;
    private String bookingCode;
    private Long eventId;
    private String eventTitle;
    private BigDecimal amount;
    private PaymentProvider provider;
    private PaymentStatus status;
    private String paymentUrl;
    private String qrCodeUrl;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private String transferContent;
    private Long remainingSeconds;
    private LocalDateTime paymentTime;
    private String errorMessage;
    private LocalDateTime createdAt;
}
