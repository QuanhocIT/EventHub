package com.eventhub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aggregated ratings summary and star breakdown for an event")
public class EventRatingSummaryResponse {

    @Schema(example = "1")
    private Long eventId;

    @Schema(example = "4.85")
    private Double averageRating;

    @Schema(example = "120")
    private Integer totalReviews;

    @Schema(example = "90")
    private Long star5Count;

    @Schema(example = "20")
    private Long star4Count;

    @Schema(example = "6")
    private Long star3Count;

    @Schema(example = "3")
    private Long star2Count;

    @Schema(example = "1")
    private Long star1Count;
}
