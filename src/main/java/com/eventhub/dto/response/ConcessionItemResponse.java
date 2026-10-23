package com.eventhub.dto.response;

import com.eventhub.entities.ConcessionItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcessionItemResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private String category;
    private String status;
    private Boolean isHotDeal;

    public static ConcessionItemResponse fromEntity(ConcessionItem c) {
        if (c == null) return null;
        return ConcessionItemResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .price(c.getPrice())
                .imageUrl(c.getImageUrl())
                .category(c.getCategory() != null ? c.getCategory().name() : "COMBO")
                .status(c.getStatus())
                .isHotDeal(c.getIsHotDeal())
                .build();
    }
}
