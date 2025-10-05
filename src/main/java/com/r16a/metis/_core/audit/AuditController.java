package com.r16a.metis._core.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {
    private final AuditService auditService;
    
    @GetMapping("/logs")
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public ResponseEntity<Page<AuditLog>> getAllAuditLogs(Pageable pageable) {
        Page<AuditLog> logs = auditService.getAllAuditLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/tenant/{tenantId}")
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByTenant(
            @PathVariable String tenantId,
            Pageable pageable
    ) {
        Page<AuditLog> logs = auditService.getAuditLogsByTenant(tenantId, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/logs/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            Pageable pageable
    ) {
        List<AuditLog> logs = auditService.getAuditLogsByEntity(entityType, entityId, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/logs/user/{performedBy}")
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(@PathVariable String performedBy) {
        List<AuditLog> logs = auditService.getAuditLogsByUser(performedBy);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/logs/operation/{operation}")
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public ResponseEntity<List<AuditLog>> getAuditLogsByOperation(@PathVariable AuditLog.Operation operation) {
        List<AuditLog> logs = auditService.getAuditLogsByOperation(operation);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/logs/date-range")
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Pageable pageable
    ) {
        Page<AuditLog> logs = auditService.getAuditLogsByDateRange(start, end, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/logs/tenant/{tenantId}/date-range")
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByTenantAndDateRange(
            @PathVariable String tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Pageable pageable
    ) {
        Page<AuditLog> logs = auditService.getAuditLogsByTenantAndDateRange(tenantId, start, end, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/logs/entity-type/{entityType}/tenant/{tenantId}")
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getAuditLogsByEntityTypeAndTenant(
            @PathVariable String entityType,
            @PathVariable String tenantId
    ) {
        List<AuditLog> logs = auditService.getAuditLogsByEntityTypeAndTenant(entityType, tenantId);
        return ResponseEntity.ok(logs);
    }
}
