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
@Schema(description = "Compact organizer summary item for follow lists and directories")
public class OrganizerSummaryResponse {

    @Schema(example = "2")
    private Long organizerId;

    @Schema(example = "organizer")
    private String username;

    @Schema(example = "Nexus Tech Media & Events")
    private String organizationName;

    @Schema(example = "https://images.unsplash.com/photo-brand-logo.png")
    private String logoUrl;

    @Schema(example = "true")
    private Boolean verified;

    @Schema(example = "1540")
    private Long followerCount;

    @Schema(example = "12")
    private Long totalEvents;

    @Schema(example = "4.92")
    private Double averageRating;

    @Schema(example = "true")
    private Boolean isFollowed;
}
