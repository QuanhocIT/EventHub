package com.eventhub.services;

import com.eventhub.common.PageResponse;
import com.eventhub.dto.request.UpdateOrganizerProfileRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.dto.response.OrganizerProfileResponse;
import com.eventhub.dto.response.OrganizerSummaryResponse;
import com.eventhub.entities.Event;
import com.eventhub.entities.OrganizerFollow;
import com.eventhub.entities.OrganizerProfile;
import com.eventhub.entities.User;
import com.eventhub.enums.EventStatus;
import com.eventhub.enums.UserRole;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ForbiddenException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.mappers.EventMapper;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.OrganizerFollowRepository;
import com.eventhub.repositories.OrganizerProfileRepository;
import com.eventhub.repositories.UserRepository;
import com.eventhub.services.impl.OrganizerServiceImpl;
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
class OrganizerServiceTest {

    @Mock
    private OrganizerProfileRepository organizerProfileRepository;

    @Mock
    private OrganizerFollowRepository organizerFollowRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Spy
    private EventMapper eventMapper = new EventMapper();

    @InjectMocks
    private OrganizerServiceImpl organizerService;

    private User sampleOrganizer;
    private User sampleCustomer;
    private OrganizerProfile sampleProfile;
    private OrganizerFollow sampleFollow;
    private Event sampleEvent;

    @BeforeEach
    void setUp() {
        sampleOrganizer = User.builder()
                .id(2L)
                .username("organizer_pro")
                .fullName("Tech Events Vietnam")
                .email("contact@techevents.vn")
                .phoneNumber("0912345678")
                .avatarUrl("https://example.com/logo.png")
                .role(UserRole.ROLE_ORGANIZER)
                .createdAt(LocalDateTime.now().minusMonths(6))
                .build();

        sampleCustomer = User.builder()
                .id(10L)
                .username("alice_customer")
                .fullName("Alice Nguyen")
                .email("alice@example.com")
                .role(UserRole.ROLE_CUSTOMER)
                .build();

        sampleProfile = OrganizerProfile.builder()
                .id(1L)
                .user(sampleOrganizer)
                .organizationName("Nexus Tech Media & Events")
                .bio("Premier technology events organizer")
                .logoUrl("https://example.com/nexus-logo.png")
                .bannerUrl("https://example.com/nexus-banner.png")
                .website("https://nexustech.events")
                .contactEmail("info@nexustech.events")
                .contactPhone("+84 28 3822 9999")
                .verified(true)
                .build();

        sampleFollow = OrganizerFollow.builder()
                .id(100L)
                .user(sampleCustomer)
                .organizer(sampleOrganizer)
                .createdAt(LocalDateTime.now())
                .build();

        sampleEvent = Event.builder()
                .id(50L)
                .title("Vietnam Cloud Summit")
                .organizer(sampleOrganizer)
                .status(EventStatus.PUBLISHED)
                .startTime(LocalDateTime.now().plusWeeks(1))
                .endTime(LocalDateTime.now().plusWeeks(1).plusHours(6))
                .averageRating(4.8)
                .reviewCount(15)
                .build();
    }

    @Test
    @DisplayName("Get organizer profile successfully with profile entity and aggregated stats")
    void getOrganizerProfile_Success_WithProfile() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleOrganizer));
        when(organizerProfileRepository.findByUserId(2L)).thenReturn(Optional.of(sampleProfile));
        when(eventRepository.countByOrganizerId(2L)).thenReturn(5L);
        when(eventRepository.countByOrganizerIdAndStatus(2L, EventStatus.PUBLISHED)).thenReturn(2L);
        when(organizerFollowRepository.countByOrganizerId(2L)).thenReturn(120L);
        when(eventRepository.getAverageRatingByOrganizerId(2L)).thenReturn(4.85);
        when(userRepository.findByUsername("alice_customer")).thenReturn(Optional.of(sampleCustomer));
        when(organizerFollowRepository.existsByUserIdAndOrganizerId(10L, 2L)).thenReturn(true);

        OrganizerProfileResponse response = organizerService.getOrganizerProfile(2L, "alice_customer");

        assertThat(response).isNotNull();
        assertThat(response.getOrganizerId()).isEqualTo(2L);
        assertThat(response.getOrganizationName()).isEqualTo("Nexus Tech Media & Events");
        assertThat(response.getVerified()).isTrue();
        assertThat(response.getTotalEvents()).isEqualTo(5L);
        assertThat(response.getActiveEvents()).isEqualTo(2L);
        assertThat(response.getFollowerCount()).isEqualTo(120L);
        assertThat(response.getAverageRating()).isEqualTo(4.85);
        assertThat(response.getIsFollowed()).isTrue();
    }

    @Test
    @DisplayName("Get organizer profile successfully when no profile entity exists yet (fallback to User fields)")
    void getOrganizerProfile_Success_WithoutProfileEntity() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleOrganizer));
        when(organizerProfileRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(eventRepository.countByOrganizerId(2L)).thenReturn(1L);
        when(eventRepository.countByOrganizerIdAndStatus(2L, EventStatus.PUBLISHED)).thenReturn(1L);
        when(organizerFollowRepository.countByOrganizerId(2L)).thenReturn(0L);
        when(eventRepository.getAverageRatingByOrganizerId(2L)).thenReturn(0.0);

        OrganizerProfileResponse response = organizerService.getOrganizerProfile(2L, null);

        assertThat(response).isNotNull();
        assertThat(response.getOrganizerId()).isEqualTo(2L);
        assertThat(response.getOrganizationName()).isEqualTo("Tech Events Vietnam");
        assertThat(response.getVerified()).isFalse();
        assertThat(response.getIsFollowed()).isFalse();
    }

    @Test
    @DisplayName("Get organizer profile throws ResourceNotFound when organizer does not exist")
    void getOrganizerProfile_ThrowsNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizerService.getOrganizerProfile(999L, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Organizer user not found");
    }

    @Test
    @DisplayName("Update organizer profile successfully when caller has ROLE_ORGANIZER")
    void updateMyOrganizerProfile_Success() {
        UpdateOrganizerProfileRequest request = UpdateOrganizerProfileRequest.builder()
                .organizationName("Updated Tech Events")
                .bio("Updated bio")
                .logoUrl("https://example.com/new-logo.png")
                .website("https://updated.events")
                .contactEmail("contact@updated.events")
                .build();

        when(userRepository.findByUsername("organizer_pro")).thenReturn(Optional.of(sampleOrganizer));
        when(organizerProfileRepository.findByUserId(2L)).thenReturn(Optional.of(sampleProfile));
        when(organizerProfileRepository.save(any(OrganizerProfile.class))).thenReturn(sampleProfile);

        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleOrganizer));
        when(eventRepository.countByOrganizerId(2L)).thenReturn(3L);
        when(eventRepository.countByOrganizerIdAndStatus(2L, EventStatus.PUBLISHED)).thenReturn(1L);
        when(organizerFollowRepository.countByOrganizerId(2L)).thenReturn(50L);
        when(eventRepository.getAverageRatingByOrganizerId(2L)).thenReturn(4.5);

        OrganizerProfileResponse response = organizerService.updateMyOrganizerProfile(request, "organizer_pro");

        assertThat(response).isNotNull();
        verify(organizerProfileRepository).save(any(OrganizerProfile.class));
        assertThat(sampleProfile.getOrganizationName()).isEqualTo("Updated Tech Events");
        assertThat(sampleProfile.getWebsite()).isEqualTo("https://updated.events");
    }

    @Test
    @DisplayName("Update organizer profile throws Forbidden when user is not an organizer")
    void updateMyOrganizerProfile_ThrowsForbidden_WhenNotOrganizer() {
        UpdateOrganizerProfileRequest request = UpdateOrganizerProfileRequest.builder()
                .organizationName("Illegal Org")
                .build();

        when(userRepository.findByUsername("alice_customer")).thenReturn(Optional.of(sampleCustomer));

        assertThatThrownBy(() -> organizerService.updateMyOrganizerProfile(request, "alice_customer"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Only organizers can configure");

        verify(organizerProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Follow organizer successfully when user follows an organizer")
    void followOrganizer_Success() {
        when(userRepository.findByUsername("alice_customer")).thenReturn(Optional.of(sampleCustomer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleOrganizer));
        when(organizerFollowRepository.existsByUserIdAndOrganizerId(10L, 2L)).thenReturn(false);

        organizerService.followOrganizer(2L, "alice_customer");

        verify(organizerFollowRepository).save(any(OrganizerFollow.class));
    }

    @Test
    @DisplayName("Follow organizer throws BadRequest when user attempts to follow themselves")
    void followOrganizer_ThrowsBadRequest_WhenFollowingSelf() {
        when(userRepository.findByUsername("alice_customer")).thenReturn(Optional.of(sampleCustomer));

        assertThatThrownBy(() -> organizerService.followOrganizer(10L, "alice_customer"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("You cannot follow yourself");

        verify(organizerFollowRepository, never()).save(any());
    }

    @Test
    @DisplayName("Follow organizer is idempotent when user is already following")
    void followOrganizer_Idempotent_WhenAlreadyFollowing() {
        when(userRepository.findByUsername("alice_customer")).thenReturn(Optional.of(sampleCustomer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleOrganizer));
        when(organizerFollowRepository.existsByUserIdAndOrganizerId(10L, 2L)).thenReturn(true);

        organizerService.followOrganizer(2L, "alice_customer");

        verify(organizerFollowRepository, never()).save(any());
    }

    @Test
    @DisplayName("Unfollow organizer successfully removes follow relation")
    void unfollowOrganizer_Success() {
        when(userRepository.findByUsername("alice_customer")).thenReturn(Optional.of(sampleCustomer));
        when(organizerFollowRepository.findByUserIdAndOrganizerId(10L, 2L)).thenReturn(Optional.of(sampleFollow));

        organizerService.unfollowOrganizer(2L, "alice_customer");

        verify(organizerFollowRepository).delete(sampleFollow);
    }

    @Test
    @DisplayName("Get followed organizers returns paginated PageResponse")
    void getFollowedOrganizers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrganizerFollow> followPage = new PageImpl<>(List.of(sampleFollow), pageable, 1);

        when(userRepository.findByUsername("alice_customer")).thenReturn(Optional.of(sampleCustomer));
        when(organizerFollowRepository.findByUserIdOrderByCreatedAtDesc(10L, pageable)).thenReturn(followPage);
        when(organizerProfileRepository.findByUserId(2L)).thenReturn(Optional.of(sampleProfile));
        when(organizerFollowRepository.countByOrganizerId(2L)).thenReturn(100L);
        when(eventRepository.countByOrganizerId(2L)).thenReturn(4L);
        when(eventRepository.getAverageRatingByOrganizerId(2L)).thenReturn(4.9);

        PageResponse<OrganizerSummaryResponse> response = organizerService.getFollowedOrganizers("alice_customer", pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getOrganizationName()).isEqualTo("Nexus Tech Media & Events");
        assertThat(response.getContent().get(0).getIsFollowed()).isTrue();
    }

    @Test
    @DisplayName("Get organizer events returns paginated published events")
    void getOrganizerEvents_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventPage = new PageImpl<>(List.of(sampleEvent), pageable, 1);

        when(userRepository.existsById(2L)).thenReturn(true);
        when(eventRepository.findByOrganizerIdAndStatusOrderByStartTimeDesc(2L, EventStatus.PUBLISHED, pageable)).thenReturn(eventPage);

        PageResponse<EventResponse> response = organizerService.getOrganizerEvents(2L, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("Vietnam Cloud Summit");
    }

    @Test
    @DisplayName("Verify organizer sets verified status to true/false successfully")
    void verifyOrganizer_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleOrganizer));
        when(organizerProfileRepository.findByUserId(2L)).thenReturn(Optional.of(sampleProfile));
        when(organizerProfileRepository.save(sampleProfile)).thenReturn(sampleProfile);

        when(eventRepository.countByOrganizerId(2L)).thenReturn(5L);
        when(eventRepository.countByOrganizerIdAndStatus(2L, EventStatus.PUBLISHED)).thenReturn(2L);
        when(organizerFollowRepository.countByOrganizerId(2L)).thenReturn(100L);
        when(eventRepository.getAverageRatingByOrganizerId(2L)).thenReturn(4.8);

        OrganizerProfileResponse response = organizerService.verifyOrganizer(2L, true);

        assertThat(response).isNotNull();
        assertThat(sampleProfile.getVerified()).isTrue();
        verify(organizerProfileRepository).save(sampleProfile);
    }
}
