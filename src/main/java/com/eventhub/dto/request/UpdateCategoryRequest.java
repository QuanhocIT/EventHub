package com.eventhub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request body for updating an existing Category")
public class UpdateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Schema(example = "Music & Festival", description = "Updated category name")
    private String name;

    @Schema(example = "Live concerts and annual musical festivals")
    private String description;

    @Schema(example = "https://example.com/icons/music-v2.png")
    private String iconUrl;

    @Schema(example = "true", description = "Whether the category is active")
    private Boolean active;
}
