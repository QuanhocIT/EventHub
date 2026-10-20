package com.eventhub.services;

import com.eventhub.dto.request.ChangePasswordRequest;
import com.eventhub.dto.request.UpdateProfileRequest;
import com.eventhub.dto.response.UserProfileResponse;
import com.eventhub.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserProfileResponse getMyProfile(String currentUsername);

    UserProfileResponse updateMyProfile(String currentUsername, UpdateProfileRequest request);

    void changePassword(String currentUsername, ChangePasswordRequest request);

    Page<UserProfileResponse> getAllUsers(String keyword, UserRole role, Boolean enabled, Pageable pageable);

    UserProfileResponse getUserById(Long id);

    UserProfileResponse updateUserStatus(Long id, boolean enabled, String currentAdminUsername);

    UserProfileResponse updateUserRole(Long id, UserRole newRole, String currentAdminUsername);
}
