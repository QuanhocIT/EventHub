package com.eventhub.repositories;

import com.eventhub.entities.EventSeat;
import com.eventhub.enums.EventSeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventSeatRepository extends JpaRepository<EventSeat, Long> {

    List<EventSeat> findByEventIdOrderBySeatSeatRowAscSeatSeatNumberAsc(Long eventId);

    List<EventSeat> findByEventIdAndStatus(Long eventId, EventSeatStatus status);

    long countByEventIdAndStatus(Long eventId, EventSeatStatus status);

    boolean existsByEventId(Long eventId);

    Optional<EventSeat> findByEventIdAndSeatId(Long eventId, Long seatId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT es FROM EventSeat es WHERE es.id = :id")
    Optional<EventSeat> findByIdWithLock(@Param("id") Long id);
}
