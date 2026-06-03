package com.smartasset.audit.service;

import com.smartasset.audit.domain.AuditLog;
import com.smartasset.audit.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public List<AuditLog> findAll() { return repository.findAll(); }

    public List<AuditLog> findByEntity(String entityType, UUID entityId) {
        return repository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public AuditLog log(AuditLog auditLog) {
        AuditLog saved = repository.save(auditLog);
        log.info("Audit log created: {} on {}/{}", saved.getAction(), saved.getEntityType(), saved.getEntityId());
        return saved;
    }
}
