package com.eventhub.repositories;

import com.eventhub.entities.ConcessionItem;
import com.eventhub.enums.ConcessionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConcessionItemRepository extends JpaRepository<ConcessionItem, Long> {

    List<ConcessionItem> findByStatus(String status);

    List<ConcessionItem> findByCategoryAndStatus(ConcessionCategory category, String status);

    List<ConcessionItem> findByIsHotDealTrueAndStatus(String status);
}
