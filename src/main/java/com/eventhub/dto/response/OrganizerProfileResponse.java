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
@Schema(description = "Detailed public organizer profile response with statistics")
public class OrganizerProfileResponse {

    @Schema(example = "2")
    private Long organizerId;

    @Schema(example = "organizer")
    private String username;

    @Schema(example = "Nexus Tech Media & Events")
    private String organizationName;

    @Schema(example = "Leading technology event organizers in Southeast Asia since 2018.")
    private String bio;

    @Schema(example = "https://images.unsplash.com/photo-brand-logo.png")
    private String logoUrl;

    @Schema(example = "https://images.unsplash.com/photo-brand-banner.png")
    private String bannerUrl;

    @Schema(example = "https://nexustech.events")
    private String website;

    @Schema(example = "contact@nexustech.events")
    private String contactEmail;

    @Schema(example = "+84 28 3822 9999")
    private String contactPhone;

    @Schema(example = "https://facebook.com/nexustechevents")
    private String facebookUrl;

    @Schema(example = "https://linkedin.com/company/nexus-tech-events")
    private String linkedinUrl;

    @Schema(example = "true", description = "True if organizer has verified badge from EventHub platform")
    private Boolean verified;

    @Schema(example = "12", description = "Total events hosted by this organizer")
    private Long totalEvents;

    @Schema(example = "4", description = "Number of currently active/published events")
    private Long activeEvents;

    @Schema(example = "1540", description = "Number of users following this organizer")
    private Long followerCount;

    @Schema(example = "4.92", description = "Average event rating across all events hosted")
    private Double averageRating;

    @Schema(example = "true", description = "True if current authenticated user is following this organizer")
    private Boolean isFollowed;

    @Schema(example = "2026-01-15T08:00:00")
    private LocalDateTime memberSince;
}
