package com.eventhub.repositories;

import com.eventhub.entities.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByVenueIdOrderBySeatRowAscSeatNumberAsc(Long venueId);

    long countByVenueId(Long venueId);
}
