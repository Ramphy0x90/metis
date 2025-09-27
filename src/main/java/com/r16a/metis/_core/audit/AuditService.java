package com.r16a.metis._core.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCreate(String entityType, UUID entityId, Object entity, String tenantId) {
        try {
            String newValues = objectMapper.writeValueAsString(entity);
            String performedBy = getCurrentUser();
            
            AuditLog auditLog = new AuditLog();
            auditLog.setOperation(AuditLog.Operation.CREATE);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setNewValues(newValues);
            auditLog.setPerformedBy(performedBy);
            auditLog.setTenantId(tenantId);
            auditLog.setDescription(String.format("Created %s with ID: %s", entityType, entityId));
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log created for {} operation on {} with ID: {}", 
                AuditLog.Operation.CREATE, entityType, entityId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize entity for audit log", e);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUpdate(String entityType, UUID entityId, Object oldEntity, Object newEntity, String tenantId) {
        try {
            String oldValues = objectMapper.writeValueAsString(oldEntity);
            String newValues = objectMapper.writeValueAsString(newEntity);
            String performedBy = getCurrentUser();
            
            AuditLog auditLog = new AuditLog();
            auditLog.setOperation(AuditLog.Operation.UPDATE);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setOldValues(oldValues);
            auditLog.setNewValues(newValues);
            auditLog.setPerformedBy(performedBy);
            auditLog.setTenantId(tenantId);
            auditLog.setDescription(String.format("Updated %s with ID: %s", entityType, entityId));
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log created for {} operation on {} with ID: {}", 
                AuditLog.Operation.UPDATE, entityType, entityId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize entity for audit log", e);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logDelete(String entityType, UUID entityId, Object entity, String tenantId) {
        try {
            String oldValues = objectMapper.writeValueAsString(entity);
            String performedBy = getCurrentUser();
            
            AuditLog auditLog = new AuditLog();
            auditLog.setOperation(AuditLog.Operation.DELETE);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setOldValues(oldValues);
            auditLog.setPerformedBy(performedBy);
            auditLog.setTenantId(tenantId);
            auditLog.setDescription(String.format("Deleted %s with ID: %s", entityType, entityId));
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log created for {} operation on {} with ID: {}", 
                AuditLog.Operation.DELETE, entityType, entityId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize entity for audit log", e);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logBulkDelete(String entityType, String description, String tenantId) {
        String performedBy = getCurrentUser();
        
        AuditLog auditLog = new AuditLog();
        auditLog.setOperation(AuditLog.Operation.DELETE);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(null); // Bulk operation
        auditLog.setPerformedBy(performedBy);
        auditLog.setTenantId(tenantId);
        auditLog.setDescription(description);
        
        auditLogRepository.save(auditLog);
        log.debug("Audit log created for bulk delete operation: {}", description);
    }
    
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "SYSTEM";
    }
}
