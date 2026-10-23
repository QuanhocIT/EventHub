package com.eventhub.repositories;

import com.eventhub.entities.Movie;
import com.eventhub.enums.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findBySlug(String slug);

    List<Movie> findByStatusOrderByReleaseDateDesc(MovieStatus status);

    List<Movie> findByTitleContainingIgnoreCase(String keyword);
}
