package com.eventhub.repositories;

import com.eventhub.entities.Ticket;
import com.eventhub.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketCode(String ticketCode);

    List<Ticket> findByBookingId(Long bookingId);

    Page<Ticket> findByBookingUserId(Long userId, Pageable pageable);

    boolean existsByBookingUserIdAndEventIdAndStatus(Long userId, Long eventId, TicketStatus status);

    long countByEventIdAndStatus(Long eventId, TicketStatus status);

    long countByEventId(Long eventId);

    long countByEventOrganizerId(Long organizerId);

    long countByEventOrganizerIdAndStatus(Long organizerId, TicketStatus status);

    long countByStatus(TicketStatus status);
}
