package com.eventhub.dto.request;

import com.eventhub.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating user role")
public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    @Schema(example = "ROLE_ORGANIZER")
    private UserRole role;
}
