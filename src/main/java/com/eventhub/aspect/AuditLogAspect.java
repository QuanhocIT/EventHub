package com.eventhub.aspect;

import com.eventhub.entities.AuditLog;
import com.eventhub.services.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(auditable)")
    public Object profileMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();
        String username = "ANONYMOUS";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            username = auth.getName();
        }

        String clientIp = extractClientIp();
        String userAgent = extractUserAgent();
        String entityId = extractEntityId(joinPoint.getArgs());

        Object result;
        try {
            result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            AuditLog logEntry = AuditLog.builder()
                    .username(username)
                    .action(auditable.action())
                    .entityType(auditable.entityType())
                    .entityId(entityId)
                    .description(auditable.description().isBlank() ? auditable.action() : auditable.description())
                    .ipAddress(clientIp)
                    .userAgent(userAgent)
                    .status("SUCCESS")
                    .executionTimeMs(executionTime)
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogService.logAction(logEntry);
            return result;
        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;

            AuditLog logEntry = AuditLog.builder()
                    .username(username)
                    .action(auditable.action())
                    .entityType(auditable.entityType())
                    .entityId(entityId)
                    .description(auditable.description().isBlank() ? auditable.action() : auditable.description())
                    .ipAddress(clientIp)
                    .userAgent(userAgent)
                    .status("FAILED")
                    .errorMessage(ex.getMessage())
                    .executionTimeMs(executionTime)
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogService.logAction(logEntry);
            throw ex;
        }
    }

    private String extractClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }

    private String extractUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ua = request.getHeader("User-Agent");
                if (ua != null && ua.length() > 500) {
                    return ua.substring(0, 497) + "...";
                }
                return ua;
            }
        } catch (Exception ignored) {
        }
        return "Internal";
    }

    private String extractEntityId(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof Long || arg instanceof Integer) {
                return String.valueOf(arg);
            }
            if (arg instanceof String && !((String) arg).contains(" ") && ((String) arg).length() <= 64) {
                return (String) arg;
            }
        }
        return null;
    }
}
