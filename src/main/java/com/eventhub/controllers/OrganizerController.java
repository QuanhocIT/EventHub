package com.eventhub.controllers;

import com.eventhub.common.ApiResponse;
import com.eventhub.common.PageResponse;
import com.eventhub.dto.request.UpdateOrganizerProfileRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.dto.response.OrganizerProfileResponse;
import com.eventhub.dto.response.OrganizerSummaryResponse;
import com.eventhub.services.OrganizerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/organizers")
@RequiredArgsConstructor
@Tag(name = "Organizers", description = "APIs for organizer public profiles, brand pages, follow/unfollow, and admin verification")
public class OrganizerController {

    private final OrganizerService organizerService;

    @Operation(summary = "Get organizer public profile", description = "Public profile with follower count, total events hosted, and average rating")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizerProfileResponse>> getOrganizerProfile(
            @PathVariable Long id,
            Principal principal
    ) {
        String currentUsername = principal != null ? principal.getName() : null;
        OrganizerProfileResponse profile = organizerService.getOrganizerProfile(id, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Organizer profile retrieved successfully", profile));
    }

    @Operation(summary = "Get published events by organizer", description = "Public paginated list of published events hosted by this organizer")
    @GetMapping("/{id}/events")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> getOrganizerEvents(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
        PageResponse<EventResponse> events = organizerService.getOrganizerEvents(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @Operation(summary = "Get current organizer profile", description = "Retrieve profile for logged-in organizer")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<OrganizerProfileResponse>> getMyOrganizerProfile(Principal principal) {
        OrganizerProfileResponse profile = organizerService.getMyOrganizerProfile(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Organizer profile retrieved successfully", profile));
    }

    @Operation(summary = "Update organizer brand profile", description = "Allows organizers to update bio, logo, banner, and social media links")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<OrganizerProfileResponse>> updateMyOrganizerProfile(
            @Valid @RequestBody UpdateOrganizerProfileRequest request,
            Principal principal
    ) {
        OrganizerProfileResponse updated = organizerService.updateMyOrganizerProfile(request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Organizer profile updated successfully", updated));
    }

    @Operation(summary = "Follow an organizer", description = "Current authenticated user follows the specified organizer")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/follow")
    public ResponseEntity<ApiResponse<Void>> followOrganizer(
            @PathVariable Long id,
            Principal principal
    ) {
        organizerService.followOrganizer(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Successfully followed organizer", null));
    }

    @Operation(summary = "Unfollow an organizer", description = "Current authenticated user unfollows the specified organizer")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/follow")
    public ResponseEntity<ApiResponse<Void>> unfollowOrganizer(
            @PathVariable Long id,
            Principal principal
    ) {
        organizerService.unfollowOrganizer(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Successfully unfollowed organizer", null));
    }

    @Operation(summary = "Get list of followed organizers", description = "Paginated list of organizers followed by current user")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/following")
    public ResponseEntity<ApiResponse<PageResponse<OrganizerSummaryResponse>>> getFollowedOrganizers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<OrganizerSummaryResponse> response = organizerService.getFollowedOrganizers(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Admin: Verify or unverify an organizer", description = "Grants or removes blue verification checkmark badge")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<OrganizerProfileResponse>> verifyOrganizer(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean verified
    ) {
        OrganizerProfileResponse response = organizerService.verifyOrganizer(id, verified);
        return ResponseEntity.ok(ApiResponse.success("Organizer verification status updated successfully", response));
    }
}
