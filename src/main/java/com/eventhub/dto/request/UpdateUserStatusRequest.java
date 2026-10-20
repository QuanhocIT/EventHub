package com.eventhub.dto.request;

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
@Schema(description = "Request body for updating user status (lock/unlock)")
public class UpdateUserStatusRequest {

    @NotNull(message = "Enabled status is required")
    @Schema(example = "false", description = "true to enable, false to lock account")
    private Boolean enabled;
}
