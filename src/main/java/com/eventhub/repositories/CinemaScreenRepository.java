package com.eventhub.repositories;

import com.eventhub.entities.CinemaScreen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CinemaScreenRepository extends JpaRepository<CinemaScreen, Long> {

    List<CinemaScreen> findByComplexId(Long complexId);
}
