package com.r16a.metis.booking.services;

import com.r16a.metis._core.audit.AuditService;
import com.r16a.metis._core.exceptions.TenantNotFoundException;
import com.r16a.metis.booking.models.TenantService;
import com.r16a.metis.booking.repositories.TenantServiceRepository;
import com.r16a.metis.identity.repositories.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TenantServiceService {
    
    private final TenantServiceRepository tenantServiceRepository;
    private final TenantRepository tenantRepository;
    private final AuditService auditService;
    
    public Page<TenantService> getAllServices(Pageable pageable) {
        return tenantServiceRepository.findAll(pageable);
    }
    
    public TenantService getServiceById(UUID serviceId) {
        return tenantServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
    }
    
    public TenantService createService(UUID tenantId, String title, String description, int durationMinutes, BigDecimal price) {
        // Validate tenant exists
        if (!tenantRepository.existsById(tenantId)) {
            throw new TenantNotFoundException(tenantId);
        }
        
        TenantService service = new TenantService();
        service.setTitle(title);
        service.setDescription(description);
        service.setDurationMinutes(durationMinutes);
        service.setPrice(price);
        service.setTenant(tenantRepository.findById(tenantId).orElseThrow());
        
        TenantService savedService = tenantServiceRepository.save(service);
        
        // Audit log the creation
        auditService.logCreate("TenantService", savedService.getId(), savedService, tenantId.toString());
        
        log.info("Created service: {} with ID: {} for tenant: {}", title, savedService.getId(), tenantId);
        return savedService;
    }
    
    public TenantService updateService(UUID serviceId, String name, String description, int durationMinutes, BigDecimal price) {
        TenantService service = tenantServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        
        // Store old values for audit
        TenantService oldService = new TenantService();
        oldService.setId(service.getId());
        oldService.setTitle(service.getTitle());
        oldService.setDescription(service.getDescription());
        oldService.setDurationMinutes(service.getDurationMinutes());
        oldService.setPrice(service.getPrice());
        
        service.setTitle(name);
        service.setDescription(description);
        service.setDurationMinutes(durationMinutes);
        service.setPrice(price);
        
        TenantService updatedService = tenantServiceRepository.save(service);
        
        // Audit log the update
        String tenantId = updatedService.getTenant() != null ? updatedService.getTenant().getId().toString() : null;
        auditService.logUpdate("TenantService", updatedService.getId(), oldService, updatedService, tenantId);
        
        log.info("Updated service: {} with ID: {}", name, updatedService.getId());
        return updatedService;
    }
    
    public void deleteService(UUID serviceId) {
        TenantService service = tenantServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        
        String tenantId = service.getTenant() != null ? service.getTenant().getId().toString() : null;
        
        tenantServiceRepository.deleteById(serviceId);
        
        // Audit log the deletion
        auditService.logDelete("TenantService", serviceId, service, tenantId);
        
        log.info("Deleted service with ID: {}", serviceId);
    }
    
    public Page<TenantService> getServicesByTenant(UUID tenantId, Pageable pageable) {
        return tenantServiceRepository.findByTenantId(tenantId, pageable);
    }
}
