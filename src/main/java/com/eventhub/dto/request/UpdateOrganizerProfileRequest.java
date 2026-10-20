package com.eventhub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating Organizer brand profile")
public class UpdateOrganizerProfileRequest {

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 150, message = "Organization name must be between 2 and 150 characters")
    @Schema(example = "Nexus Tech Media & Events")
    private String organizationName;

    @Size(max = 2000, message = "Bio cannot exceed 2000 characters")
    @Schema(example = "Leading technology event organizers in Southeast Asia since 2018.")
    private String bio;

    @Schema(example = "https://images.unsplash.com/photo-brand-logo.png")
    private String logoUrl;

    @Schema(example = "https://images.unsplash.com/photo-brand-banner.png")
    private String bannerUrl;

    @Schema(example = "https://nexustech.events")
    private String website;

    @Email(message = "Contact email must be valid")
    @Schema(example = "contact@nexustech.events")
    private String contactEmail;

    @Schema(example = "+84 28 3822 9999")
    private String contactPhone;

    @Schema(example = "https://facebook.com/nexustechevents")
    private String facebookUrl;

    @Schema(example = "https://linkedin.com/company/nexus-tech-events")
    private String linkedinUrl;
}
