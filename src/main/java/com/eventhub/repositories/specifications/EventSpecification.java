package com.eventhub.repositories.specifications;

import com.eventhub.entities.Event;
import com.eventhub.enums.EventStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventSpecification {

    public static Specification<Event> filter(
            String keyword,
            Long categoryId,
            EventStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String location
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                Predicate locMatch = cb.like(cb.lower(root.get("location")), pattern);
                predicates.add(cb.or(titleMatch, descMatch, locMatch));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endTime"), endDate));
            }

            if (location != null && !location.trim().isEmpty()) {
                String locPattern = "%" + location.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("location")), locPattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
