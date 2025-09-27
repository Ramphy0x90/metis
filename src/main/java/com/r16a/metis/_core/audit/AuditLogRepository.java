package com.r16a.metis._core.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId);
    
    List<AuditLog> findByTenantId(String tenantId);
    
    List<AuditLog> findByPerformedBy(String performedBy);
    
    List<AuditLog> findByOperation(AuditLog.Operation operation);
    
    Page<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.tenantId = :tenantId AND a.timestamp BETWEEN :start AND :end")
    Page<AuditLog> findByTenantIdAndTimestampBetween(
        @Param("tenantId") String tenantId, 
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end, 
        Pageable pageable
    );
    
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.tenantId = :tenantId")
    List<AuditLog> findByEntityTypeAndTenantId(@Param("entityType") String entityType, @Param("tenantId") String tenantId);
}
