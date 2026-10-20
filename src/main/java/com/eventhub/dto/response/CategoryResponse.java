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
@Schema(description = "Category response data")
public class CategoryResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Music & Concert")
    private String name;

    @Schema(example = "music-concert")
    private String slug;

    @Schema(example = "Live music shows, concerts, and festivals")
    private String description;

    @Schema(example = "https://example.com/icons/music.png")
    private String iconUrl;

    @Schema(example = "true")
    private boolean active;

    @Schema(example = "2026-10-21T09:00:00")
    private LocalDateTime createdAt;
}
