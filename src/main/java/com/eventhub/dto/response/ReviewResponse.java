package com.eventhub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Event review response details")
public class ReviewResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "1")
    private Long eventId;

    @Schema(example = "3")
    private Long userId;

    @Schema(example = "John Doe")
    private String reviewerName;

    @Schema(example = "https://images.unsplash.com/photo-user.jpg")
    private String reviewerAvatar;

    @Schema(example = "5")
    private Integer rating;

    @Schema(example = "Outstanding event organization and insightful speakers!")
    private String comment;

    @Schema(example = "true", description = "True if reviewer has verified confirmed booking or checked in")
    private Boolean isVerifiedAttendee;

    @Schema(example = "2026-10-21T10:00:00")
    private LocalDateTime createdAt;

    @Schema(example = "2026-10-21T10:00:00")
    private LocalDateTime updatedAt;
}
