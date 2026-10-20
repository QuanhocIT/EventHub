package com.eventhub.repositories;

import com.eventhub.entities.Booking;
import com.eventhub.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingCode(String bookingCode);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByEventId(Long eventId, Pageable pageable);

    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime now);

    boolean existsByUserIdAndEventIdAndStatus(Long userId, Long eventId, BookingStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.event.organizer.id = :organizerId AND b.status = :status")
    java.math.BigDecimal sumTotalAmountByOrganizerIdAndStatus(@org.springframework.data.repository.query.Param("organizerId") Long organizerId, @org.springframework.data.repository.query.Param("status") BookingStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.event.id = :eventId AND b.status = :status")
    java.math.BigDecimal sumTotalAmountByEventIdAndStatus(@org.springframework.data.repository.query.Param("eventId") Long eventId, @org.springframework.data.repository.query.Param("status") BookingStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.status = :status")
    java.math.BigDecimal sumTotalAmountByStatus(@org.springframework.data.repository.query.Param("status") BookingStatus status);

    long countByStatus(BookingStatus status);

    long countByEventOrganizerId(Long organizerId);
}
