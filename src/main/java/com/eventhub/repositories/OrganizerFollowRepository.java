package com.eventhub.repositories;

import com.eventhub.entities.OrganizerFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizerFollowRepository extends JpaRepository<OrganizerFollow, Long> {

    Optional<OrganizerFollow> findByUserIdAndOrganizerId(Long userId, Long organizerId);

    boolean existsByUserIdAndOrganizerId(Long userId, Long organizerId);

    long countByOrganizerId(Long organizerId);

    Page<OrganizerFollow> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    void deleteByUserIdAndOrganizerId(Long userId, Long organizerId);
}
