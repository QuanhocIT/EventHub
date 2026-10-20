package com.eventhub.repositories;

import com.eventhub.entities.OrganizerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizerProfileRepository extends JpaRepository<OrganizerProfile, Long> {

    Optional<OrganizerProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
