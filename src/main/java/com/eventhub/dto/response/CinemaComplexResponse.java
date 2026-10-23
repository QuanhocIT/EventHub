package com.eventhub.dto.response;

import com.eventhub.entities.CinemaComplex;
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
public class CinemaComplexResponse {
    private Long id;
    private String name;
    private String city;
    private String address;
    private String phone;
    private Integer totalScreens;
    private String imageUrl;

    public static CinemaComplexResponse fromEntity(CinemaComplex c) {
        if (c == null) return null;
        return CinemaComplexResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .city(c.getCity())
                .address(c.getAddress())
                .phone(c.getPhone())
                .totalScreens(c.getTotalScreens())
                .imageUrl(c.getImageUrl())
                .build();
    }
}
