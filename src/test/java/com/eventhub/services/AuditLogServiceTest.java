package com.eventhub.services;

import com.eventhub.common.PageResponse;
import com.eventhub.dto.response.AuditLogResponse;
import com.eventhub.entities.AuditLog;
import com.eventhub.repositories.AuditLogRepository;
import com.eventhub.services.impl.AuditLogServiceImpl;
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
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private AuditLog sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog = AuditLog.builder()
                .id(1L)
                .userId(1L)
                .username("admin")
                .action("USER_STATUS_UPDATE")
                .entityType("User")
                .entityId("5")
                .description("Admin updated user enabled status")
                .ipAddress("127.0.0.1")
                .userAgent("JUnit 5")
                .status("SUCCESS")
                .executionTimeMs(15L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Log action persists audit log entity successfully")
    void logAction_Success() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(sampleLog);

        auditLogService.logAction(sampleLog);

        verify(auditLogRepository).save(sampleLog);
    }

    @Test
    @DisplayName("Get audit logs with filters returns paginated PageResponse")
    void getAuditLogs_WithFilters_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> page = new PageImpl<>(List.of(sampleLog), pageable, 1);

        when(auditLogRepository.findAll(org.mockito.ArgumentMatchers.<Specification<AuditLog>>any(), any(Pageable.class))).thenReturn(page);

        PageResponse<AuditLogResponse> response = auditLogService.getAuditLogs("USER_STATUS_UPDATE", "admin", "User", pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getAction()).isEqualTo("USER_STATUS_UPDATE");
        assertThat(response.getContent().get(0).getUsername()).isEqualTo("admin");
    }
}
