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
    private final AuditLogRepository auditLogRepository;
    
    @GetMapping("/logs")
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public ResponseEntity<Page<AuditLog>> getAllAuditLogs(Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findAll(pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/logs/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable UUID entityId) {
        List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/logs/tenant/{tenantId}")
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getAuditLogsByTenant(@PathVariable String tenantId) {
        List<AuditLog> logs = auditLogRepository.findByTenantId(tenantId);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/logs/user/{performedBy}")
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(@PathVariable String performedBy) {
        List<AuditLog> logs = auditLogRepository.findByPerformedBy(performedBy);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/logs/operation/{operation}")
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public ResponseEntity<List<AuditLog>> getAuditLogsByOperation(@PathVariable AuditLog.Operation operation) {
        List<AuditLog> logs = auditLogRepository.findByOperation(operation);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/logs/date-range")
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByTimestampBetween(start, end, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/logs/tenant/{tenantId}/date-range")
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByTenantAndDateRange(
            @PathVariable String tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByTenantIdAndTimestampBetween(tenantId, start, end, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/logs/entity-type/{entityType}/tenant/{tenantId}")
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getAuditLogsByEntityTypeAndTenant(
            @PathVariable String entityType,
            @PathVariable String tenantId) {
        List<AuditLog> logs = auditLogRepository.findByEntityTypeAndTenantId(entityType, tenantId);
        return ResponseEntity.ok(logs);
    }
}
