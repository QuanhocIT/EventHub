package com.eventhub.dto.response;

import com.eventhub.enums.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Event data response")
public class EventResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Tech Conference 2026")
    private String title;

    @Schema(example = "Annual technology gathering with keynote speakers")
    private String description;

    @Schema(example = "Convention Center, Hanoi")
    private String location;

    @Schema(example = "2026-10-01T09:00:00")
    private LocalDateTime startTime;

    @Schema(example = "2026-10-01T17:00:00")
    private LocalDateTime endTime;

    @Schema(example = "500")
    private Integer capacity;

    @Schema(example = "49.99")
    private BigDecimal price;

    @Schema(example = "PUBLISHED")
    private EventStatus status;

    @Schema(example = "2026-09-25T10:00:00")
    private LocalDateTime createdAt;

    @Schema(example = "2026-09-25T10:00:00")
    private LocalDateTime updatedAt;

    @Schema(example = "1")
    private Long organizerId;

    @Schema(example = "John Doe")
    private String organizerName;
}
