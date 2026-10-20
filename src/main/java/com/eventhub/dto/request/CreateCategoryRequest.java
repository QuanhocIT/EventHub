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
@Schema(description = "Request body for creating a new Category")
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Schema(example = "Music & Concert", description = "Name of the category")
    private String name;

    @Schema(example = "Live music shows, concerts, and festivals")
    private String description;

    @Schema(example = "https://example.com/icons/music.png", description = "Icon or banner URL")
    private String iconUrl;
}
