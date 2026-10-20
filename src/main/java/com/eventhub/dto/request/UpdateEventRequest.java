package com.eventhub.dto.request;

import com.eventhub.enums.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating an existing Event")
public class UpdateEventRequest {

    @NotBlank(message = "Event title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    @Schema(example = "Tech Conference 2026 - Updated")
    private String title;

    @Schema(example = "Updated description with new speakers")
    private String description;

    @Schema(example = "Grand Ballroom, Convention Center")
    private String location;

    @NotNull(message = "Start time is required")
    @Schema(example = "2026-10-01T09:30:00")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Schema(example = "2026-10-01T17:30:00")
    private LocalDateTime endTime;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Schema(example = "600")
    private Integer capacity;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    @Schema(example = "59.99")
    private BigDecimal price;

    @Schema(example = "PUBLISHED")
    private EventStatus status;

    @Schema(example = "1", description = "ID of the category")
    private Long categoryId;
}
