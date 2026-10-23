package com.eventhub.entities;

import com.eventhub.enums.MovieAgeRating;
import com.eventhub.enums.MovieStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "movies")
public class Movie extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String director;

    @Column(name = "actors_cast", length = 255)
    private String actorsCast;

    @Column(length = 100)
    private String genre;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_rating", nullable = false, length = 10)
    @Builder.Default
    private MovieAgeRating ageRating = MovieAgeRating.P;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MovieStatus status = MovieStatus.NOW_SHOWING;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    @Column(name = "format_display", length = 50)
    @Builder.Default
    private String formatDisplay = "2D, IMAX";

    @Column(length = 50)
    @Builder.Default
    private String language = "Phụ đề Tiếng Việt";

    @Column(name = "imdb_rating")
    @Builder.Default
    private Double imdbRating = 8.5;
}
