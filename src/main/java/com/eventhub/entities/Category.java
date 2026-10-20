package com.eventhub.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_categories_name", columnNames = {"name"}),
                @UniqueConstraint(name = "uk_categories_slug", columnNames = {"slug"})
        }
)
public class Category extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 120)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
