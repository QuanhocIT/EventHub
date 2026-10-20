package com.eventhub.dto.request;

import com.eventhub.enums.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
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
@Schema(description = "Request body for creating a new Event")
public class CreateEventRequest {

    @NotBlank(message = "Event title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    @Schema(example = "Tech Conference 2026", description = "Title of the event")
    private String title;

    @Schema(example = "Annual technology gathering with keynote speakers and workshops")
    private String description;

    @Schema(example = "Convention Center, Hanoi")
    private String location;

    @NotNull(message = "Start time is required")
    @FutureOrPresent(message = "Start time must be in the present or future")
    @Schema(example = "2026-10-01T09:00:00")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Schema(example = "2026-10-01T17:00:00")
    private LocalDateTime endTime;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Schema(example = "500", description = "Maximum number of attendees")
    private Integer capacity;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    @Schema(example = "49.99", description = "Ticket price (0 for free event)")
    private BigDecimal price;

    @Schema(example = "PUBLISHED", description = "Initial status (defaults to DRAFT if null)")
    private EventStatus status;

    @Schema(example = "1", description = "ID of the category")
    private Long categoryId;
}
