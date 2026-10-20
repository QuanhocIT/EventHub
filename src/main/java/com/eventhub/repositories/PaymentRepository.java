package com.eventhub.repositories;

import com.eventhub.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionCode(String transactionCode);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findFirstByBookingIdOrderByCreatedAtDesc(Long bookingId);
}
