package com.eventhub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating an existing review")
public class UpdateReviewRequest {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5 stars")
    @Max(value = 5, message = "Rating must be between 1 and 5 stars")
    @Schema(example = "4", description = "Updated star rating from 1 to 5")
    private Integer rating;

    @NotBlank(message = "Review comment cannot be blank")
    @Size(max = 2000, message = "Review comment cannot exceed 2000 characters")
    @Schema(example = "Updated feedback after receiving slides and recordings.", description = "Updated review feedback text")
    private String comment;
}
