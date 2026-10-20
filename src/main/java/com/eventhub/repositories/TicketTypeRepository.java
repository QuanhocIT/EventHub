package com.eventhub.repositories;

import com.eventhub.entities.TicketType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    List<TicketType> findByEventIdAndIsActiveTrue(Long eventId);

    List<TicketType> findByEventId(Long eventId);

    Optional<TicketType> findByIdAndEventId(Long id, Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketType t WHERE t.id = :id AND t.event.id = :eventId")
    Optional<TicketType> findByIdAndEventIdWithLock(@Param("id") Long id, @Param("eventId") Long eventId);
}
