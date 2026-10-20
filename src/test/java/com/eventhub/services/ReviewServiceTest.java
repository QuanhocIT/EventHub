package com.eventhub.services;

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
import com.eventhub.services.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Spy
    private ReviewMapper reviewMapper = new ReviewMapper();

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User sampleUser;
    private User otherUser;
    private Event sampleEvent;
    private Review sampleReview;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("john_doe")
                .fullName("John Doe")
                .email("john@example.com")
                .avatarUrl("https://example.com/avatar.png")
                .build();

        otherUser = User.builder()
                .id(2L)
                .username("jane_smith")
                .fullName("Jane Smith")
                .email("jane@example.com")
                .build();

        sampleEvent = Event.builder()
                .id(10L)
                .title("Tech Conference 2026")
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().plusDays(1))
                .averageRating(0.0)
                .reviewCount(0)
                .build();

        sampleReview = Review.builder()
                .id(100L)
                .event(sampleEvent)
                .user(sampleUser)
                .rating(5)
                .comment("Excellent conference, well organized!")
                .isVerifiedAttendee(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Create review successfully when user has confirmed booking")
    void createReview_Success_WithConfirmedBooking() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .rating(5)
                .comment("Great experience!")
                .build();

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(sampleEvent));
        when(reviewRepository.existsByUserIdAndEventId(1L, 10L)).thenReturn(false);
        when(bookingRepository.existsByUserIdAndEventIdAndStatus(1L, 10L, BookingStatus.CONFIRMED)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenReturn(sampleReview);
        when(reviewRepository.countByEventId(10L)).thenReturn(1L);
        when(reviewRepository.getAverageRatingByEventId(10L)).thenReturn(5.0);

        ReviewResponse response = reviewService.createReview(10L, request, "john_doe");

        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getReviewerName()).isEqualTo("John Doe");
        assertThat(response.getIsVerifiedAttendee()).isTrue();

        verify(eventRepository).save(sampleEvent);
        assertThat(sampleEvent.getAverageRating()).isEqualTo(5.0);
        assertThat(sampleEvent.getReviewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Create review successfully when user has used/checked-in ticket")
    void createReview_Success_WithCheckedInTicket() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .rating(4)
                .comment("Good atmosphere and content")
                .build();

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(sampleEvent));
        when(reviewRepository.existsByUserIdAndEventId(1L, 10L)).thenReturn(false);
        when(bookingRepository.existsByUserIdAndEventIdAndStatus(1L, 10L, BookingStatus.CONFIRMED)).thenReturn(false);
        when(ticketRepository.existsByBookingUserIdAndEventIdAndStatus(1L, 10L, TicketStatus.USED)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenReturn(sampleReview);
        when(reviewRepository.countByEventId(10L)).thenReturn(1L);
        when(reviewRepository.getAverageRatingByEventId(10L)).thenReturn(4.0);

        ReviewResponse response = reviewService.createReview(10L, request, "john_doe");

        assertThat(response).isNotNull();
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("Create review throws BadRequest when event has not started yet")
    void createReview_ThrowsBadRequest_WhenEventNotStarted() {
        sampleEvent.setStartTime(LocalDateTime.now().plusDays(2));
        CreateReviewRequest request = CreateReviewRequest.builder()
                .rating(5)
                .comment("Premature review")
                .build();

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(sampleEvent));

        assertThatThrownBy(() -> reviewService.createReview(10L, request, "john_doe"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot review an event that has not started yet");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create review throws BadRequest when user already submitted a review")
    void createReview_ThrowsBadRequest_WhenAlreadyReviewed() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .rating(5)
                .comment("Duplicate review")
                .build();

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(sampleEvent));
        when(reviewRepository.existsByUserIdAndEventId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(10L, request, "john_doe"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already reviewed this event");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create review throws BadRequest when user is not a verified attendee")
    void createReview_ThrowsBadRequest_WhenNotVerifiedAttendee() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .rating(5)
                .comment("Fake review without ticket")
                .build();

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(sampleEvent));
        when(reviewRepository.existsByUserIdAndEventId(1L, 10L)).thenReturn(false);
        when(bookingRepository.existsByUserIdAndEventIdAndStatus(1L, 10L, BookingStatus.CONFIRMED)).thenReturn(false);
        when(ticketRepository.existsByBookingUserIdAndEventIdAndStatus(1L, 10L, TicketStatus.USED)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.createReview(10L, request, "john_doe"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only verified attendees");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update review successfully when user is the review author")
    void updateReview_Success_WhenOwner() {
        UpdateReviewRequest request = UpdateReviewRequest.builder()
                .rating(4)
                .comment("Updated: Content was good, minor audio delay.")
                .build();

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(reviewRepository.findById(100L)).thenReturn(Optional.of(sampleReview));
        when(reviewRepository.save(sampleReview)).thenReturn(sampleReview);
        when(reviewRepository.countByEventId(10L)).thenReturn(1L);
        when(reviewRepository.getAverageRatingByEventId(10L)).thenReturn(4.0);

        ReviewResponse response = reviewService.updateReview(100L, request, "john_doe");

        assertThat(response).isNotNull();
        assertThat(sampleReview.getRating()).isEqualTo(4);
        assertThat(sampleReview.getComment()).isEqualTo("Updated: Content was good, minor audio delay.");
        verify(eventRepository).save(sampleEvent);
    }

    @Test
    @DisplayName("Update review throws Forbidden when user is not the review author")
    void updateReview_ThrowsForbidden_WhenNotOwner() {
        UpdateReviewRequest request = UpdateReviewRequest.builder()
                .rating(4)
                .comment("Illegal update")
                .build();

        when(userRepository.findByUsername("jane_smith")).thenReturn(Optional.of(otherUser));
        when(reviewRepository.findById(100L)).thenReturn(Optional.of(sampleReview));

        assertThatThrownBy(() -> reviewService.updateReview(100L, request, "jane_smith"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not authorized to edit this review");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete review successfully when caller is the review owner")
    void deleteReview_Success_WhenOwner() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(reviewRepository.findById(100L)).thenReturn(Optional.of(sampleReview));
        when(reviewRepository.countByEventId(10L)).thenReturn(0L);
        when(reviewRepository.getAverageRatingByEventId(10L)).thenReturn(0.0);

        reviewService.deleteReview(100L, "john_doe", false);

        verify(reviewRepository).delete(sampleReview);
        verify(eventRepository).save(sampleEvent);
        assertThat(sampleEvent.getAverageRating()).isEqualTo(0.0);
        assertThat(sampleEvent.getReviewCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Delete review successfully when caller is administrator")
    void deleteReview_Success_WhenAdmin() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(otherUser));
        when(reviewRepository.findById(100L)).thenReturn(Optional.of(sampleReview));
        when(reviewRepository.countByEventId(10L)).thenReturn(0L);
        when(reviewRepository.getAverageRatingByEventId(10L)).thenReturn(0.0);

        reviewService.deleteReview(100L, "admin", true);

        verify(reviewRepository).delete(sampleReview);
        verify(eventRepository).save(sampleEvent);
    }

    @Test
    @DisplayName("Delete review throws Forbidden when caller is not owner and not admin")
    void deleteReview_ThrowsForbidden_WhenNotOwnerAndNotAdmin() {
        when(userRepository.findByUsername("jane_smith")).thenReturn(Optional.of(otherUser));
        when(reviewRepository.findById(100L)).thenReturn(Optional.of(sampleReview));

        assertThatThrownBy(() -> reviewService.deleteReview(100L, "jane_smith", false))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not authorized to delete this review");

        verify(reviewRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Get reviews by event returns paginated PageResponse")
    void getReviewsByEvent_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(List.of(sampleReview), pageable, 1);

        when(eventRepository.existsById(10L)).thenReturn(true);
        when(reviewRepository.findByEventIdOrderByCreatedAtDesc(10L, pageable)).thenReturn(reviewPage);

        PageResponse<ReviewResponse> result = reviewService.getReviewsByEvent(10L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getReviewerName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Get reviews by event throws ResourceNotFound when event does not exist")
    void getReviewsByEvent_ThrowsNotFound_WhenEventMissing() {
        Pageable pageable = PageRequest.of(0, 10);
        when(eventRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.getReviewsByEvent(999L, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Get event rating summary returns correct star breakdown and average")
    void getEventRatingSummary_Success() {
        when(eventRepository.existsById(10L)).thenReturn(true);
        when(reviewRepository.countByEventIdAndRating(10L, 5)).thenReturn(10L);
        when(reviewRepository.countByEventIdAndRating(10L, 4)).thenReturn(3L);
        when(reviewRepository.countByEventIdAndRating(10L, 3)).thenReturn(1L);
        when(reviewRepository.countByEventIdAndRating(10L, 2)).thenReturn(0L);
        when(reviewRepository.countByEventIdAndRating(10L, 1)).thenReturn(0L);
        when(reviewRepository.countByEventId(10L)).thenReturn(14L);
        when(reviewRepository.getAverageRatingByEventId(10L)).thenReturn(4.6428);

        EventRatingSummaryResponse summary = reviewService.getEventRatingSummary(10L);

        assertThat(summary).isNotNull();
        assertThat(summary.getEventId()).isEqualTo(10L);
        assertThat(summary.getAverageRating()).isEqualTo(4.64);
        assertThat(summary.getTotalReviews()).isEqualTo(14);
        assertThat(summary.getStar5Count()).isEqualTo(10L);
        assertThat(summary.getStar4Count()).isEqualTo(3L);
        assertThat(summary.getStar3Count()).isEqualTo(1L);
        assertThat(summary.getStar2Count()).isEqualTo(0L);
        assertThat(summary.getStar1Count()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Get user review for event returns ReviewResponse when exists")
    void getUserReviewForEvent_ReturnsReview_WhenExists() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(eventRepository.existsById(10L)).thenReturn(true);
        when(reviewRepository.findByUserIdAndEventId(1L, 10L)).thenReturn(Optional.of(sampleReview));

        ReviewResponse response = reviewService.getUserReviewForEvent(10L, "john_doe");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("Get user review for event returns null when user has not reviewed")
    void getUserReviewForEvent_ReturnsNull_WhenNotExists() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(eventRepository.existsById(10L)).thenReturn(true);
        when(reviewRepository.findByUserIdAndEventId(1L, 10L)).thenReturn(Optional.empty());

        ReviewResponse response = reviewService.getUserReviewForEvent(10L, "john_doe");

        assertThat(response).isNull();
    }
}
