package com.eventhub.dto.response;

import com.eventhub.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile response")
public class UserProfileResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "john_doe")
    private String username;

    @Schema(example = "john@example.com")
    private String email;

    @Schema(example = "John Doe")
    private String fullName;

    @Schema(example = "0987654321")
    private String phoneNumber;

    @Schema(example = "https://images.unsplash.com/photo-1534528741775-53994a69daeb")
    private String avatarUrl;

    @Schema(example = "ROLE_CUSTOMER")
    private UserRole role;

    @Schema(example = "true")
    private boolean enabled;

    @Schema(example = "2026-10-21T09:00:00")
    private LocalDateTime createdAt;

    @Schema(example = "2026-10-21T09:00:00")
    private LocalDateTime updatedAt;
}
