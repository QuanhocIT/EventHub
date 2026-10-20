package com.eventhub.services.impl;

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
import com.eventhub.services.OrganizerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizerServiceImpl implements OrganizerService {

    private final OrganizerProfileRepository organizerProfileRepository;
    private final OrganizerFollowRepository organizerFollowRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    public OrganizerProfileResponse getOrganizerProfile(Long organizerId, String currentUsername) {
        log.debug("Fetching organizer profile for user ID: {} (viewer: {})", organizerId, currentUsername);

        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer user not found with ID: " + organizerId));

        OrganizerProfile profile = organizerProfileRepository.findByUserId(organizerId)
                .orElse(null);

        long totalEvents = eventRepository.countByOrganizerId(organizerId);
        long activeEvents = eventRepository.countByOrganizerIdAndStatus(organizerId, EventStatus.PUBLISHED);
        long followerCount = organizerFollowRepository.countByOrganizerId(organizerId);

        Double rawAvg = eventRepository.getAverageRatingByOrganizerId(organizerId);
        double roundedAvg = rawAvg != null ? Math.round(rawAvg * 100.0) / 100.0 : 0.0;

        boolean isFollowed = false;
        if (currentUsername != null && !currentUsername.isBlank()) {
            isFollowed = userRepository.findByUsername(currentUsername)
                    .map(currentUser -> organizerFollowRepository.existsByUserIdAndOrganizerId(currentUser.getId(), organizerId))
                    .orElse(false);
        }

        String orgName = profile != null && profile.getOrganizationName() != null && !profile.getOrganizationName().isBlank()
                ? profile.getOrganizationName()
                : (organizer.getFullName() != null && !organizer.getFullName().isBlank() ? organizer.getFullName() : organizer.getUsername());

        return OrganizerProfileResponse.builder()
                .organizerId(organizer.getId())
                .username(organizer.getUsername())
                .organizationName(orgName)
                .bio(profile != null ? profile.getBio() : null)
                .logoUrl(profile != null && profile.getLogoUrl() != null ? profile.getLogoUrl() : organizer.getAvatarUrl())
                .bannerUrl(profile != null ? profile.getBannerUrl() : null)
                .website(profile != null ? profile.getWebsite() : null)
                .contactEmail(profile != null ? profile.getContactEmail() : organizer.getEmail())
                .contactPhone(profile != null ? profile.getContactPhone() : organizer.getPhoneNumber())
                .facebookUrl(profile != null ? profile.getFacebookUrl() : null)
                .linkedinUrl(profile != null ? profile.getLinkedinUrl() : null)
                .verified(profile != null && Boolean.TRUE.equals(profile.getVerified()))
                .totalEvents(totalEvents)
                .activeEvents(activeEvents)
                .followerCount(followerCount)
                .averageRating(roundedAvg)
                .isFollowed(isFollowed)
                .memberSince(organizer.getCreatedAt())
                .build();
    }

    @Override
    public OrganizerProfileResponse getMyOrganizerProfile(String currentUsername) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + currentUsername));
        return getOrganizerProfile(user.getId(), currentUsername);
    }

    @Override
    @Transactional
    public OrganizerProfileResponse updateMyOrganizerProfile(UpdateOrganizerProfileRequest request, String currentUsername) {
        log.info("Updating organizer profile for user: {}", currentUsername);

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + currentUsername));

        if (user.getRole() != UserRole.ROLE_ORGANIZER && user.getRole() != UserRole.ROLE_ADMIN) {
            throw new ForbiddenException("Only organizers can configure an organization brand profile");
        }

        OrganizerProfile profile = organizerProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> OrganizerProfile.builder()
                        .user(user)
                        .verified(false)
                        .build());

        profile.setOrganizationName(request.getOrganizationName().trim());
        profile.setBio(request.getBio());
        profile.setLogoUrl(request.getLogoUrl());
        profile.setBannerUrl(request.getBannerUrl());
        profile.setWebsite(request.getWebsite());
        profile.setContactEmail(request.getContactEmail());
        profile.setContactPhone(request.getContactPhone());
        profile.setFacebookUrl(request.getFacebookUrl());
        profile.setLinkedinUrl(request.getLinkedinUrl());

        organizerProfileRepository.save(profile);
        log.info("Organizer profile saved for user: {}", currentUsername);

        return getOrganizerProfile(user.getId(), currentUsername);
    }

    @Override
    @Transactional
    public void followOrganizer(Long organizerId, String currentUsername) {
        log.info("User '{}' attempting to follow organizer ID {}", currentUsername, organizerId);

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + currentUsername));

        if (currentUser.getId().equals(organizerId)) {
            throw new BadRequestException("You cannot follow yourself");
        }

        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer user not found with ID: " + organizerId));

        if (organizerFollowRepository.existsByUserIdAndOrganizerId(currentUser.getId(), organizer.getId())) {
            log.debug("User '{}' already follows organizer ID {}", currentUsername, organizerId);
            return;
        }

        OrganizerFollow follow = OrganizerFollow.builder()
                .user(currentUser)
                .organizer(organizer)
                .build();

        organizerFollowRepository.save(follow);
        log.info("User '{}' is now following organizer '{}'", currentUsername, organizer.getUsername());
    }

    @Override
    @Transactional
    public void unfollowOrganizer(Long organizerId, String currentUsername) {
        log.info("User '{}' attempting to unfollow organizer ID {}", currentUsername, organizerId);

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + currentUsername));

        organizerFollowRepository.findByUserIdAndOrganizerId(currentUser.getId(), organizerId)
                .ifPresent(follow -> {
                    organizerFollowRepository.delete(follow);
                    log.info("User '{}' unfollowed organizer ID {}", currentUsername, organizerId);
                });
    }

    @Override
    public PageResponse<OrganizerSummaryResponse> getFollowedOrganizers(String currentUsername, Pageable pageable) {
        log.debug("Fetching followed organizers for user '{}'", currentUsername);

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + currentUsername));

        Page<OrganizerFollow> followPage = organizerFollowRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable);

        Page<OrganizerSummaryResponse> summaryPage = followPage.map(follow -> {
            User org = follow.getOrganizer();
            Optional<OrganizerProfile> profileOpt = organizerProfileRepository.findByUserId(org.getId());

            String orgName = profileOpt.map(OrganizerProfile::getOrganizationName)
                    .filter(name -> !name.isBlank())
                    .orElse(org.getFullName() != null && !org.getFullName().isBlank() ? org.getFullName() : org.getUsername());

            String logoUrl = profileOpt.map(OrganizerProfile::getLogoUrl)
                    .filter(url -> !url.isBlank())
                    .orElse(org.getAvatarUrl());

            boolean verified = profileOpt.map(OrganizerProfile::getVerified).orElse(false);
            long followerCount = organizerFollowRepository.countByOrganizerId(org.getId());
            long totalEvents = eventRepository.countByOrganizerId(org.getId());
            Double rawAvg = eventRepository.getAverageRatingByOrganizerId(org.getId());
            double roundedAvg = rawAvg != null ? Math.round(rawAvg * 100.0) / 100.0 : 0.0;

            return OrganizerSummaryResponse.builder()
                    .organizerId(org.getId())
                    .username(org.getUsername())
                    .organizationName(orgName)
                    .logoUrl(logoUrl)
                    .verified(verified)
                    .followerCount(followerCount)
                    .totalEvents(totalEvents)
                    .averageRating(roundedAvg)
                    .isFollowed(true)
                    .build();
        });

        return PageResponse.from(summaryPage);
    }

    @Override
    public PageResponse<EventResponse> getOrganizerEvents(Long organizerId, Pageable pageable) {
        log.debug("Fetching published events for organizer ID: {}", organizerId);

        if (!userRepository.existsById(organizerId)) {
            throw new ResourceNotFoundException("Organizer user not found with ID: " + organizerId);
        }

        Page<Event> eventPage = eventRepository.findByOrganizerIdAndStatusOrderByStartTimeDesc(
                organizerId, EventStatus.PUBLISHED, pageable);

        return PageResponse.from(eventPage.map(eventMapper::toResponse));
    }

    @Override
    @Transactional
    @com.eventhub.aspect.Auditable(action = "ORGANIZER_VERIFY", entityType = "OrganizerProfile", description = "Admin updated organizer verification status")
    public OrganizerProfileResponse verifyOrganizer(Long organizerId, boolean verified) {
        log.info("Admin setting verification status of organizer ID {} to {}", organizerId, verified);

        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer user not found with ID: " + organizerId));

        OrganizerProfile profile = organizerProfileRepository.findByUserId(organizerId)
                .orElseGet(() -> OrganizerProfile.builder()
                        .user(organizer)
                        .organizationName(organizer.getFullName() != null ? organizer.getFullName() : organizer.getUsername())
                        .build());

        profile.setVerified(verified);
        organizerProfileRepository.save(profile);
        log.info("Organizer ID {} verified status updated to {}", organizerId, verified);

        return getOrganizerProfile(organizerId, null);
    }
}
