package com.eventhub.dto.response;

import com.eventhub.entities.Showtime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowtimeResponse {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private String moviePoster;
    private String ageRating;
    private Long complexId;
    private String complexName;
    private String city;
    private Long screenId;
    private String screenName;
    private String screenType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double basePrice;
    private String status;

    public static ShowtimeResponse fromEntity(Showtime s) {
        if (s == null) return null;
        return ShowtimeResponse.builder()
                .id(s.getId())
                .movieId(s.getMovie() != null ? s.getMovie().getId() : null)
                .movieTitle(s.getMovie() != null ? s.getMovie().getTitle() : null)
                .moviePoster(s.getMovie() != null ? s.getMovie().getPosterUrl() : null)
                .ageRating(s.getMovie() != null && s.getMovie().getAgeRating() != null ? s.getMovie().getAgeRating().getCode() : "P")
                .complexId(s.getScreen() != null && s.getScreen().getComplex() != null ? s.getScreen().getComplex().getId() : null)
                .complexName(s.getScreen() != null && s.getScreen().getComplex() != null ? s.getScreen().getComplex().getName() : null)
                .city(s.getScreen() != null && s.getScreen().getComplex() != null ? s.getScreen().getComplex().getCity() : null)
                .screenId(s.getScreen() != null ? s.getScreen().getId() : null)
                .screenName(s.getScreen() != null ? s.getScreen().getScreenName() : null)
                .screenType(s.getScreen() != null && s.getScreen().getScreenType() != null ? s.getScreen().getScreenType().name() : "STANDARD_2D")
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .basePrice(s.getBasePrice())
                .status(s.getStatus())
                .build();
    }
}
