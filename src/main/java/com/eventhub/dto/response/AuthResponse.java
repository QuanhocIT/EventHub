package com.eventhub.dto.response;

import com.eventhub.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response with JWT token")
public class AuthResponse {

    @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Builder.Default
    @Schema(example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(example = "86400000", description = "Token expiration in milliseconds")
    private Long expiresIn;

    @Schema(example = "admin")
    private String username;

    @Schema(example = "admin@eventhub.com")
    private String email;

    @Schema(example = "ROLE_ADMIN")
    private UserRole role;
}
