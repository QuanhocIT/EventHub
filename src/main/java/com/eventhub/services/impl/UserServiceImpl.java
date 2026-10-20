package com.eventhub.services.impl;

import com.eventhub.dto.request.ChangePasswordRequest;
import com.eventhub.dto.request.UpdateProfileRequest;
import com.eventhub.dto.response.UserProfileResponse;
import com.eventhub.entities.User;
import com.eventhub.enums.UserRole;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.UserRepository;
import com.eventhub.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileResponse getMyProfile(String currentUsername) {
        log.debug("Fetching profile for user: {}", currentUsername);
        User user = findUserByUsername(currentUsername);
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(String currentUsername, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", currentUsername);
        User user = findUserByUsername(currentUsername);

        user.setFullName(request.getFullName().trim());
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }

        User updated = userRepository.save(user);
        log.info("User {} profile updated successfully", currentUsername);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void changePassword(String currentUsername, ChangePasswordRequest request) {
        log.info("Processing password change for user: {}", currentUsername);
        User user = findUserByUsername(currentUsername);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirmation password do not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be the same as current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", currentUsername);
    }

    @Override
    public Page<UserProfileResponse> getAllUsers(String keyword, UserRole role, Boolean enabled, Pageable pageable) {
        log.debug("Admin querying users - keyword: {}, role: {}, enabled: {}", keyword, role, enabled);
        return userRepository.searchUsers(keyword, role, enabled, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public UserProfileResponse getUserById(Long id) {
        log.debug("Admin fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserStatus(Long id, boolean enabled, String currentAdminUsername) {
        log.info("Admin {} updating user ID: {} status to enabled={}", currentAdminUsername, id, enabled);
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (targetUser.getUsername().equalsIgnoreCase(currentAdminUsername)) {
            throw new BadRequestException("Admin cannot disable their own account");
        }

        targetUser.setEnabled(enabled);
        User updated = userRepository.save(targetUser);
        log.info("User {} status updated to enabled={}", targetUser.getUsername(), enabled);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserRole(Long id, UserRole newRole, String currentAdminUsername) {
        log.info("Admin {} updating user ID: {} role to {}", currentAdminUsername, id, newRole);
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (targetUser.getUsername().equalsIgnoreCase(currentAdminUsername) && newRole != UserRole.ROLE_ADMIN) {
            throw new BadRequestException("Admin cannot revoke their own administrator role");
        }

        targetUser.setRole(newRole);
        User updated = userRepository.save(targetUser);
        log.info("User {} role updated to {}", targetUser.getUsername(), newRole);
        return mapToResponse(updated);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private UserProfileResponse mapToResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
