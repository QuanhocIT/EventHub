package com.eventhub.mappers;

import com.eventhub.dto.response.ReviewResponse;
import com.eventhub.entities.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        if (review == null) {
            return null;
        }

        String reviewerName = null;
        String reviewerAvatar = null;
        Long userId = null;

        if (review.getUser() != null) {
            userId = review.getUser().getId();
            reviewerName = review.getUser().getFullName() != null && !review.getUser().getFullName().isBlank()
                    ? review.getUser().getFullName()
                    : review.getUser().getUsername();
            reviewerAvatar = review.getUser().getAvatarUrl();
        }

        Long eventId = review.getEvent() != null ? review.getEvent().getId() : null;

        java.util.List<String> imageUrls = java.util.Collections.emptyList();
        if (review.getImagesJson() != null && !review.getImagesJson().isBlank()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                imageUrls = om.readValue(review.getImagesJson(), new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {});
            } catch (Exception e) {
                imageUrls = java.util.Arrays.asList(review.getImagesJson().split(","));
            }
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .eventId(eventId)
                .userId(userId)
                .reviewerName(reviewerName)
                .reviewerAvatar(reviewerAvatar)
                .rating(review.getRating())
                .comment(review.getComment())
                .imageUrls(imageUrls)
                .likesCount(review.getLikesCount() != null ? review.getLikesCount() : 0)
                .isLiked(false)
                .organizerReply(review.getOrganizerReply())
                .organizerReplyAt(review.getOrganizerReplyAt())
                .isVerifiedAttendee(review.getIsVerifiedAttendee())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
