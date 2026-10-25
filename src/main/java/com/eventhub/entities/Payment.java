package com.eventhub.entities;

import com.eventhub.enums.PaymentProvider;
import com.eventhub.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_transaction_code", columnNames = {"transaction_code"})
        },
        indexes = {
                @Index(name = "idx_payments_booking", columnList = "booking_id"),
                @Index(name = "idx_payments_idempotency", columnList = "idempotency_key"),
                @Index(name = "idx_payments_status", columnList = "status")
        }
)
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "transaction_code", nullable = false, length = 64)
    private String transactionCode;

    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentProvider provider = PaymentProvider.MOCK_SANDBOX;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "payment_url", length = 500)
    private String paymentUrl;

    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;

    @Column(name = "error_message", length = 500)
    private String errorMessage;
}
