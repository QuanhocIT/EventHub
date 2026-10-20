package com.eventhub.repositories;

import com.eventhub.entities.Event;
import com.eventhub.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE " +
           "(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR e.status = :status)")
    Page<Event> searchEvents(@Param("keyword") String keyword, @Param("status") EventStatus status, Pageable pageable);

    long countByStatus(EventStatus status);

    long countByOrganizerId(Long organizerId);

    long countByOrganizerIdAndStatus(Long organizerId, EventStatus status);

    @Query("SELECT e FROM Event e WHERE e.startTime >= :now ORDER BY e.startTime ASC")
    Page<Event> findUpcomingEvents(@Param("now") LocalDateTime now, Pageable pageable);
}
