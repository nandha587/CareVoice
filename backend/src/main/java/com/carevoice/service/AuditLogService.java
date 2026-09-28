package com.carevoice.service;

import com.carevoice.entity.AuditLog;
import com.carevoice.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(UUID userId, String action, String entityType, String entityId, String metadata) {
        try {
            AuditLog auditLog = new AuditLog(userId, action, entityType, entityId, metadata);
            auditLogRepository.save(auditLog);
            log.info("[AUDIT] User: {} | Action: {} | Entity: {} ({}) | Details: {}",
                    userId, action, entityType, entityId, metadata);
        } catch (Exception e) {
            log.error("Failed to write audit log entry: {}", e.getMessage(), e);
        }
    }
}
