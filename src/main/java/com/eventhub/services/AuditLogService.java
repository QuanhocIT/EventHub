package com.eventhub.services;

import com.eventhub.common.PageResponse;
import com.eventhub.dto.response.AuditLogResponse;
import com.eventhub.entities.AuditLog;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    void logAction(AuditLog auditLog);

    PageResponse<AuditLogResponse> getAuditLogs(String action, String username, String entityType, Pageable pageable);
}
