package com.smartasset.audit.controller;

import com.smartasset.audit.domain.AuditLog;
import com.smartasset.audit.service.AuditService;
import com.smartasset.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(auditService.findAll()));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getByEntity(@PathVariable String entityType,
            @PathVariable UUID entityId) {
        return ResponseEntity.ok(ApiResponse.success(auditService.findByEntity(entityType, entityId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AuditLog>> log(@RequestBody AuditLog auditLog) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Audit logged", auditService.log(auditLog)));
    }

    @GetMapping("/validate/{entityType}/{entityId}/{action}")
    public ResponseEntity<ApiResponse<Boolean>> validateAction(@PathVariable String entityType,
            @PathVariable UUID entityId, @PathVariable String action) {
        List<AuditLog> logs = auditService.findByEntity(entityType, entityId);
        boolean exists = logs.stream().anyMatch(l -> l.getAction().equalsIgnoreCase(action));
        return ResponseEntity.ok(ApiResponse.success("Validation result", exists));
    }
}
