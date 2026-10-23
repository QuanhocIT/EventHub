package com.eventhub.dto.response;

import com.eventhub.entities.Movie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieResponse {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private String director;
    private String actorsCast;
    private String genre;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String ageRatingCode;
    private String ageRatingDesc;
    private String status;
    private String posterUrl;
    private String bannerUrl;
    private String trailerUrl;
    private String formatDisplay;
    private String language;
    private Double imdbRating;

    public static MovieResponse fromEntity(Movie movie) {
        if (movie == null) return null;
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .slug(movie.getSlug())
                .description(movie.getDescription())
                .director(movie.getDirector())
                .actorsCast(movie.getActorsCast())
                .genre(movie.getGenre())
                .durationMinutes(movie.getDurationMinutes())
                .releaseDate(movie.getReleaseDate())
                .ageRatingCode(movie.getAgeRating() != null ? movie.getAgeRating().getCode() : "P")
                .ageRatingDesc(movie.getAgeRating() != null ? movie.getAgeRating().getDescription() : "")
                .status(movie.getStatus() != null ? movie.getStatus().name() : "NOW_SHOWING")
                .posterUrl(movie.getPosterUrl())
                .bannerUrl(movie.getBannerUrl())
                .trailerUrl(movie.getTrailerUrl())
                .formatDisplay(movie.getFormatDisplay())
                .language(movie.getLanguage())
                .imdbRating(movie.getImdbRating())
                .build();
    }
}
