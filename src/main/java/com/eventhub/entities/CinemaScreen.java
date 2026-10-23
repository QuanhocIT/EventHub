package com.eventhub.entities;

import com.eventhub.enums.ScreenType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "cinema_screens")
public class CinemaScreen extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_complex_id", nullable = false)
    private CinemaComplex complex;

    @Column(name = "screen_name", nullable = false, length = 50)
    private String screenName;

    @Enumerated(EnumType.STRING)
    @Column(name = "screen_type", nullable = false, length = 30)
    @Builder.Default
    private ScreenType screenType = ScreenType.STANDARD_2D;

    @Column(name = "total_seats")
    @Builder.Default
    private Integer totalSeats = 120;
}
