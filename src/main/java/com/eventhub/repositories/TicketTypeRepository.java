package com.eventhub.repositories;

import com.eventhub.entities.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    List<TicketType> findByEventIdAndIsActiveTrue(Long eventId);

    List<TicketType> findByEventId(Long eventId);

    Optional<TicketType> findByIdAndEventId(Long id, Long eventId);
}
