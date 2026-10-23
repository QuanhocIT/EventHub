package com.eventhub.repositories;

import com.eventhub.entities.CinemaComplex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CinemaComplexRepository extends JpaRepository<CinemaComplex, Long> {

    List<CinemaComplex> findByCityIgnoreCaseOrderByNameAsc(String city);
}
