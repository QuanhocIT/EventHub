package com.eventhub.services.impl;

import com.eventhub.common.PageResponse;
import com.eventhub.dto.request.CreateReviewRequest;
import com.eventhub.dto.request.UpdateReviewRequest;
import com.eventhub.dto.response.EventRatingSummaryResponse;
import com.eventhub.dto.response.ReviewResponse;
import com.eventhub.entities.Event;
import com.eventhub.entities.Review;
import com.eventhub.entities.User;
import com.eventhub.enums.BookingStatus;
import com.eventhub.enums.TicketStatus;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ForbiddenException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.mappers.ReviewMapper;
import com.eventhub.repositories.BookingRepository;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.ReviewRepository;
import com.eventhub.repositories.TicketRepository;
import com.eventhub.repositories.UserRepository;
import com.eventhub.services.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse createReview(Long eventId, CreateReviewRequest request, String username) {
        log.info("User '{}' attempting to review event ID {}", username, eventId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));

        if (event.getStartTime() != null && event.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Cannot review an event that has not started yet");
        }

        if (reviewRepository.existsByUserIdAndEventId(user.getId(), event.getId())) {
            throw new BadRequestException("You have already reviewed this event. You can edit your existing review.");
        }

        boolean hasConfirmedBooking = bookingRepository.existsByUserIdAndEventIdAndStatus(
                user.getId(), event.getId(), BookingStatus.CONFIRMED);
        boolean hasCheckedInTicket = ticketRepository.existsByBookingUserIdAndEventIdAndStatus(
                user.getId(), event.getId(), TicketStatus.USED);

        if (!hasConfirmedBooking && !hasCheckedInTicket) {
            throw new BadRequestException("Only verified attendees with a confirmed booking or checked-in ticket can review this event");
        }

        Review review = Review.builder()
                .event(event)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment().trim())
                .isVerifiedAttendee(true)
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Review ID {} created for event ID {} by user '{}'", savedReview.getId(), eventId, username);

        recalculateAndSaveEventRating(event);

        return reviewMapper.toResponse(savedReview);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request, String username) {
        log.info("User '{}' attempting to update review ID {}", username, reviewId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to edit this review");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());

        Review updated = reviewRepository.save(review);
        log.info("Review ID {} updated by user '{}'", reviewId, username);

        recalculateAndSaveEventRating(review.getEvent());

        return reviewMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, String username, boolean isAdmin) {
        log.info("User '{}' (isAdmin={}) attempting to delete review ID {}", username, isAdmin, reviewId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        if (!isAdmin && !review.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to delete this review");
        }

        Event event = review.getEvent();
        reviewRepository.delete(review);
        log.info("Review ID {} deleted", reviewId);

        recalculateAndSaveEventRating(event);
    }

    @Override
    public PageResponse<ReviewResponse> getReviewsByEvent(Long eventId, Pageable pageable) {
        log.debug("Fetching reviews for event ID {}", eventId);

        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with ID: " + eventId);
        }

        Page<Review> reviewPage = reviewRepository.findByEventIdOrderByCreatedAtDesc(eventId, pageable);
        return PageResponse.from(reviewPage.map(reviewMapper::toResponse));
    }

    @Override
    public EventRatingSummaryResponse getEventRatingSummary(Long eventId) {
        log.debug("Fetching rating summary for event ID {}", eventId);

        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with ID: " + eventId);
        }

        long star5 = reviewRepository.countByEventIdAndRating(eventId, 5);
        long star4 = reviewRepository.countByEventIdAndRating(eventId, 4);
        long star3 = reviewRepository.countByEventIdAndRating(eventId, 3);
        long star2 = reviewRepository.countByEventIdAndRating(eventId, 2);
        long star1 = reviewRepository.countByEventIdAndRating(eventId, 1);
        long total = reviewRepository.countByEventId(eventId);

        Double rawAvg = reviewRepository.getAverageRatingByEventId(eventId);
        double roundedAvg = rawAvg != null ? Math.round(rawAvg * 100.0) / 100.0 : 0.0;

        return EventRatingSummaryResponse.builder()
                .eventId(eventId)
                .averageRating(roundedAvg)
                .totalReviews((int) total)
                .star5Count(star5)
                .star4Count(star4)
                .star3Count(star3)
                .star2Count(star2)
                .star1Count(star1)
                .build();
    }

    @Override
    public ReviewResponse getUserReviewForEvent(Long eventId, String username) {
        log.debug("Fetching review for event ID {} by user '{}'", eventId, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with ID: " + eventId);
        }

        return reviewRepository.findByUserIdAndEventId(user.getId(), eventId)
                .map(reviewMapper::toResponse)
                .orElse(null);
    }

    private void recalculateAndSaveEventRating(Event event) {
        long count = reviewRepository.countByEventId(event.getId());
        Double rawAvg = reviewRepository.getAverageRatingByEventId(event.getId());
        double roundedAvg = rawAvg != null ? Math.round(rawAvg * 100.0) / 100.0 : 0.0;

        event.setReviewCount((int) count);
        event.setAverageRating(roundedAvg);
        eventRepository.save(event);
        log.info("Recalculated event ID {} rating: count={}, avg={}", event.getId(), count, roundedAvg);
    }
}
