package com.eventhub.services;

import com.eventhub.dto.request.ChangePasswordRequest;
import com.eventhub.dto.request.UpdateProfileRequest;
import com.eventhub.dto.response.UserProfileResponse;
import com.eventhub.entities.User;
import com.eventhub.enums.UserRole;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.UserRepository;
import com.eventhub.services.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("$2a$10$hashedOldPassword")
                .fullName("John Doe")
                .phoneNumber("0987654321")
                .avatarUrl("https://example.com/avatar.jpg")
                .role(UserRole.ROLE_CUSTOMER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        adminUser = User.builder()
                .id(2L)
                .username("admin_boss")
                .email("admin@example.com")
                .password("$2a$10$hashedAdminPassword")
                .fullName("System Administrator")
                .role(UserRole.ROLE_ADMIN)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Should get user profile successfully")
    void getMyProfile_Success() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));

        UserProfileResponse response = userService.getMyProfile("john_doe");

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("john_doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getPhoneNumber()).isEqualTo("0987654321");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void getMyProfile_NotFound_ThrowsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyProfile("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    @DisplayName("Should update user profile successfully")
    void updateMyProfile_Success() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("Johnathan Doe")
                .phoneNumber("0912345678")
                .avatarUrl("https://example.com/new_avatar.jpg")
                .build();

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserProfileResponse response = userService.updateMyProfile("john_doe", request);

        assertThat(response.getFullName()).isEqualTo("Johnathan Doe");
        assertThat(response.getPhoneNumber()).isEqualTo("0912345678");
        assertThat(response.getAvatarUrl()).isEqualTo("https://example.com/new_avatar.jpg");
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Should change password successfully when inputs are valid")
    void changePassword_Success() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("OldPass123")
                .newPassword("NewPass123!")
                .confirmPassword("NewPass123!")
                .build();

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("OldPass123", sampleUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("NewPass123!", sampleUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("NewPass123!")).thenReturn("$2a$10$newHashedPassword");

        userService.changePassword("john_doe", request);

        assertThat(sampleUser.getPassword()).isEqualTo("$2a$10$newHashedPassword");
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Should throw BadRequestException when current password is incorrect")
    void changePassword_WrongCurrentPassword_ThrowsException() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("WrongOldPass")
                .newPassword("NewPass123!")
                .confirmPassword("NewPass123!")
                .build();

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("WrongOldPass", sampleUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("john_doe", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Current password is incorrect");
    }

    @Test
    @DisplayName("Should throw BadRequestException when new password does not match confirmation")
    void changePassword_MismatchConfirm_ThrowsException() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("OldPass123")
                .newPassword("NewPass123!")
                .confirmPassword("DifferentPass456!")
                .build();

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("OldPass123", sampleUser.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> userService.changePassword("john_doe", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("do not match");
    }

    @Test
    @DisplayName("Should throw BadRequestException when new password is identical to old password")
    void changePassword_SameAsOld_ThrowsException() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("OldPass123")
                .newPassword("OldPass123")
                .confirmPassword("OldPass123")
                .build();

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("OldPass123", sampleUser.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> userService.changePassword("john_doe", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be the same");
    }

    @Test
    @DisplayName("Should return paginated list of users for admin")
    void getAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.searchUsers("john", UserRole.ROLE_CUSTOMER, true, pageable))
                .thenReturn(new PageImpl<>(List.of(sampleUser)));

        Page<UserProfileResponse> result = userService.getAllUsers("john", UserRole.ROLE_CUSTOMER, true, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("john_doe");
    }

    @Test
    @DisplayName("Should update user enabled status successfully")
    void updateUserStatus_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserProfileResponse response = userService.updateUserStatus(1L, false, "admin_boss");

        assertThat(response.isEnabled()).isFalse();
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Should throw BadRequestException when admin attempts to lock their own account")
    void updateUserStatus_AdminLockingSelf_ThrowsException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> userService.updateUserStatus(2L, false, "admin_boss"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Admin cannot disable their own account");
    }

    @Test
    @DisplayName("Should update user role successfully")
    void updateUserRole_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserProfileResponse response = userService.updateUserRole(1L, UserRole.ROLE_ORGANIZER, "admin_boss");

        assertThat(response.getRole()).isEqualTo(UserRole.ROLE_ORGANIZER);
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Should throw BadRequestException when admin attempts to demote their own role")
    void updateUserRole_AdminDemotingSelf_ThrowsException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> userService.updateUserRole(2L, UserRole.ROLE_CUSTOMER, "admin_boss"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Admin cannot revoke their own");
    }
}
