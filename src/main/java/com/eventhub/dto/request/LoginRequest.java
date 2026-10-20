package com.eventhub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for user login")
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @Schema(example = "admin", description = "Username or email of the account")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    @Schema(example = "admin123", description = "Account password")
    private String password;
}
