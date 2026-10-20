package com.eventhub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to create a new venue")
public class CreateVenueRequest {

    @NotBlank(message = "Venue name is required")
    @Schema(example = "Hòa Bình Theater")
    private String name;

    @NotBlank(message = "Address is required")
    @Schema(example = "240 Ba Tháng Hai, Phường 12, Quận 10")
    private String address;

    @Schema(example = "Hồ Chí Minh")
    private String city;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Schema(example = "500")
    private Integer capacity;

    @Schema(description = "Number of seat rows to auto-generate (e.g. 5 rows: A, B, C, D, E)", example = "5")
    @Builder.Default
    private Integer numberOfRows = 5;

    @Schema(description = "Number of seats per row to auto-generate (e.g. 10 seats per row)", example = "10")
    @Builder.Default
    private Integer seatsPerRow = 10;

    @Schema(description = "First N rows assigned as VIP zone (e.g. 2 rows: A and B are VIP)", example = "2")
    @Builder.Default
    private Integer vipRowsCount = 2;
}
