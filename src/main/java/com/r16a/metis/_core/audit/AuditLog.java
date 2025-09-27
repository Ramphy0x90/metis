package com.r16a.metis._core.audit;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    
    public enum Operation {
        CREATE,
        UPDATE,
        DELETE
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Operation operation;
    
    @Column(nullable = false)
    private String entityType;
    
    @Column(nullable = false)
    private UUID entityId;
    
    @Column(columnDefinition = "TEXT")
    private String oldValues;
    
    @Column(columnDefinition = "TEXT")
    private String newValues;
    
    @Column(nullable = false)
    private String performedBy;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column
    private String tenantId;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
