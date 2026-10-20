package com.eventhub.controllers;

import com.eventhub.common.ApiResponse;
import com.eventhub.common.PageResponse;
import com.eventhub.dto.request.CreateReviewRequest;
import com.eventhub.dto.request.UpdateReviewRequest;
import com.eventhub.dto.response.EventRatingSummaryResponse;
import com.eventhub.dto.response.ReviewResponse;
import com.eventhub.services.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews & Ratings", description = "APIs for event ratings, attendee reviews, and score breakdown")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Get reviews for an event", description = "Public endpoint to retrieve paginated reviews for a specific event")
    @GetMapping("/api/v1/events/{eventId}/reviews")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getReviewsByEvent(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<ReviewResponse> response = reviewService.getReviewsByEvent(eventId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get event rating summary", description = "Public endpoint to retrieve average rating and 1-5 star breakdown")
    @GetMapping("/api/v1/events/{eventId}/reviews/summary")
    public ResponseEntity<ApiResponse<EventRatingSummaryResponse>> getEventRatingSummary(
            @PathVariable Long eventId
    ) {
        EventRatingSummaryResponse summary = reviewService.getEventRatingSummary(eventId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @Operation(summary = "Get current user's review for an event", description = "Check if current authenticated user has already reviewed the event")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/v1/events/{eventId}/reviews/my-review")
    public ResponseEntity<ApiResponse<ReviewResponse>> getMyReview(
            @PathVariable Long eventId,
            Principal principal
    ) {
        ReviewResponse review = reviewService.getUserReviewForEvent(eventId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("User review retrieved", review));
    }

    @Operation(summary = "Submit a review for an event", description = "Allows attendees with a confirmed booking or checked-in ticket to review an event that has started")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/v1/events/{eventId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateReviewRequest request,
            Principal principal
    ) {
        ReviewResponse created = reviewService.createReview(eventId, request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully", created));
    }

    @Operation(summary = "Update an existing review", description = "Allows author to update their review comment and star rating")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/api/v1/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request,
            Principal principal
    ) {
        ReviewResponse updated = reviewService.updateReview(reviewId, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Review updated successfully", updated));
    }

    @Operation(summary = "Delete a review", description = "Allows author or ROLE_ADMIN to delete a review")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/api/v1/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            Principal principal,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        reviewService.deleteReview(reviewId, principal.getName(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }
}
