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
@Schema(description = "Venue details response")
public class VenueResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Hòa Bình Theater")
    private String name;

    @Schema(example = "240 Ba Tháng Hai, Phường 12, Quận 10")
    private String address;

    @Schema(example = "Hồ Chí Minh")
    private String city;

    @Schema(example = "500")
    private Integer capacity;

    @Schema(example = "50")
    private Integer totalSeats;

    private LocalDateTime createdAt;
}
