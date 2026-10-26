package com.eventhub.services;

import com.eventhub.common.PageResponse;
import com.eventhub.dto.request.CreateReviewRequest;
import com.eventhub.dto.request.UpdateReviewRequest;
import com.eventhub.dto.response.EventRatingSummaryResponse;
import com.eventhub.dto.response.ReviewResponse;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    ReviewResponse createReview(Long eventId, CreateReviewRequest request, String username);

    ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request, String username);

    void deleteReview(Long reviewId, String username, boolean isAdmin);

    PageResponse<ReviewResponse> getReviewsByEvent(Long eventId, Pageable pageable);

    EventRatingSummaryResponse getEventRatingSummary(Long eventId);

    ReviewResponse getUserReviewForEvent(Long eventId, String username);

    ReviewResponse toggleLikeReview(Long reviewId, String username);

    ReviewResponse replyToReview(Long reviewId, String organizerReply, String username);
}
