package com.eventhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTypeResponse {

    private Long id;
    private Long eventId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
    private Integer minPerOrder;
    private Integer maxPerOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
