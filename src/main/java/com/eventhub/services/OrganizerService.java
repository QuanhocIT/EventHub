package com.eventhub.services;

import com.eventhub.common.PageResponse;
import com.eventhub.dto.request.UpdateOrganizerProfileRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.dto.response.OrganizerProfileResponse;
import com.eventhub.dto.response.OrganizerSummaryResponse;
import org.springframework.data.domain.Pageable;

public interface OrganizerService {

    OrganizerProfileResponse getOrganizerProfile(Long organizerId, String currentUsername);

    OrganizerProfileResponse getMyOrganizerProfile(String currentUsername);

    OrganizerProfileResponse updateMyOrganizerProfile(UpdateOrganizerProfileRequest request, String currentUsername);

    void followOrganizer(Long organizerId, String currentUsername);

    void unfollowOrganizer(Long organizerId, String currentUsername);

    PageResponse<OrganizerSummaryResponse> getFollowedOrganizers(String currentUsername, Pageable pageable);

    PageResponse<EventResponse> getOrganizerEvents(Long organizerId, Pageable pageable);

    OrganizerProfileResponse verifyOrganizer(Long organizerId, boolean verified);
}
