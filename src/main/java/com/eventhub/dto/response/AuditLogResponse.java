package com.eventhub.dto.response;

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
@Schema(description = "Security audit log entry details")
public class AuditLogResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "1")
    private Long userId;

    @Schema(example = "admin")
    private String username;

    @Schema(example = "USER_LOCK")
    private String action;

    @Schema(example = "User")
    private String entityType;

    @Schema(example = "5")
    private String entityId;

    @Schema(example = "Admin locked user account ID 5")
    private String description;

    @Schema(example = "192.168.1.100")
    private String ipAddress;

    @Schema(example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)...")
    private String userAgent;

    @Schema(example = "SUCCESS")
    private String status;

    @Schema(example = "null")
    private String errorMessage;

    @Schema(example = "42", description = "Execution duration in milliseconds")
    private Long executionTimeMs;

    @Schema(example = "2026-10-21T05:30:00")
    private LocalDateTime createdAt;
}
