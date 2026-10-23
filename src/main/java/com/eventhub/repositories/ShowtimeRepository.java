package com.eventhub.repositories;

import com.eventhub.entities.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    @Query("SELECT s FROM Showtime s JOIN FETCH s.screen sc JOIN FETCH sc.complex c " +
           "WHERE s.movie.id = :movieId " +
           "AND s.startTime >= :startRange AND s.startTime < :endRange " +
           "ORDER BY c.name ASC, s.startTime ASC")
    List<Showtime> findShowtimesByMovieAndDateRange(
            @Param("movieId") Long movieId,
            @Param("startRange") LocalDateTime startRange,
            @Param("endRange") LocalDateTime endRange
    );

    @Query("SELECT s FROM Showtime s JOIN FETCH s.movie m JOIN FETCH s.screen sc " +
           "WHERE sc.complex.id = :complexId " +
           "AND s.startTime >= :startRange AND s.startTime < :endRange " +
           "ORDER BY m.title ASC, s.startTime ASC")
    List<Showtime> findShowtimesByComplexAndDateRange(
            @Param("complexId") Long complexId,
            @Param("startRange") LocalDateTime startRange,
            @Param("endRange") LocalDateTime endRange
    );
}
