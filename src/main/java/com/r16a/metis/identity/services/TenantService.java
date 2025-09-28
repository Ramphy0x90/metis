package com.r16a.metis.identity.services;

import com.r16a.metis._core.audit.AuditService;
import com.r16a.metis._core.exceptions.TenantNotFoundException;
import com.r16a.metis.booking.repositories.BookingRepository;
import com.r16a.metis.booking.repositories.TenantServiceRepository;
import com.r16a.metis.identity.dto.TenantCreateRequest;
import com.r16a.metis.identity.dto.TenantResponse;
import com.r16a.metis.identity.dto.TenantUpdateRequest;
import com.r16a.metis.identity.models.Tenant;
import com.r16a.metis.identity.repositories.TenantRepository;
import com.r16a.metis.identity.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

record TenantCounts(long employeeCount, long customerCount) {}

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TenantService {
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final TenantServiceRepository tenantServiceRepository;
    private final BookingRepository bookingRepository;
    private final AuditService auditService;
    
    public List<TenantResponse> getAllTenants() {
        List<Tenant> tenants = tenantRepository.findAll();
        List<Object[]> tenantCounts = userRepository.getTenantCounts();
        
        // Create a map for quick lookup of counts by tenant ID
        Map<UUID, TenantCounts> countsMap = tenantCounts.stream()
                .collect(Collectors.toMap(
                    row -> (UUID) row[0],
                    row -> new TenantCounts((Long) row[1], (Long) row[2])
                ));
        
        return tenants.stream()
                .map(tenant -> mapToResponseWithCounts(tenant, countsMap.get(tenant.getId())))
                .collect(Collectors.toList());
    }
    
    public TenantResponse getTenantById(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException(id));
        return mapToResponse(tenant);
    }
    
    public TenantResponse createTenant(TenantCreateRequest request) {
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setDomain(request.getDomain());
        
        Tenant savedTenant = tenantRepository.save(tenant);
        
        // Audit log the creation
        auditService.logCreate("Tenant", savedTenant.getId(), savedTenant, savedTenant.getId().toString());
        
        log.info("Created tenant: {} with ID: {}", savedTenant.getName(), savedTenant.getId());
        return mapToResponse(savedTenant);
    }
    
    public TenantResponse updateTenant(UUID id, TenantUpdateRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException(id));
        
        // Store old values for audit
        Tenant oldTenant = new Tenant();
        oldTenant.setId(tenant.getId());
        oldTenant.setName(tenant.getName());
        oldTenant.setDomain(tenant.getDomain());
        oldTenant.setCreatedAt(tenant.getCreatedAt());
        oldTenant.setUpdatedAt(tenant.getUpdatedAt());
        
        tenant.setName(request.getName());
        tenant.setDomain(request.getDomain());
        
        Tenant updatedTenant = tenantRepository.save(tenant);
        
        // Audit log the update
        auditService.logUpdate("Tenant", updatedTenant.getId(), oldTenant, updatedTenant, updatedTenant.getId().toString());
        
        log.info("Updated tenant: {} with ID: {}", updatedTenant.getName(), updatedTenant.getId());
        return mapToResponse(updatedTenant);
    }
    
    public void deleteTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException(id));
        
        String tenantIdStr = id.toString();
        
        log.info("Starting cascade deletion for tenant: {} with ID: {}", tenant.getName(), id);
        
        // Count related entities for audit logging
        long userCount = userRepository.findByTenantId(id).size();
        long serviceCount = tenantServiceRepository.findByTenantId(id).size();
        long bookingCount = bookingRepository.findByTenantId(id).size();
        
        // Delete all related entities in proper order
        // 1. Delete bookings first (they reference users and services)
        bookingRepository.deleteByTenantId(id);
        log.info("Deleted {} bookings for tenant: {}", bookingCount, id);
        
        // 2. Delete users (this will also delete user_roles due to CASCADE)
        userRepository.deleteByTenantId(id);
        log.info("Deleted {} users for tenant: {}", userCount, id);
        
        // 3. Delete services
        tenantServiceRepository.deleteByTenantId(id);
        log.info("Deleted {} services for tenant: {}", serviceCount, id);
        
        // 4. Finally delete the tenant
        tenantRepository.deleteById(id);
        
        // Audit log the deletion with summary
        String description = String.format("Deleted tenant '%s' and all related data: %d users, %d services, %d bookings", 
            tenant.getName(), userCount, serviceCount, bookingCount);
        auditService.logBulkDelete("Tenant", description, tenantIdStr);
        
        log.info("Successfully completed cascade deletion for tenant: {} with ID: {}", tenant.getName(), id);
    }

    // TODO: Make mapping more robust
    private TenantResponse mapToResponse(Tenant tenant) {
        long employeeCount = userRepository.countEmployeesByTenantId(tenant.getId());
        long customerCount = userRepository.countCustomersByTenantId(tenant.getId());
        
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .domain(tenant.getDomain())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .employeeCount(employeeCount)
                .customerCount(customerCount)
                .build();
    }
    
    private TenantResponse mapToResponseWithCounts(Tenant tenant, TenantCounts counts) {
        long employeeCount = counts != null ? counts.employeeCount() : 0;
        long customerCount = counts != null ? counts.customerCount() : 0;
        
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .domain(tenant.getDomain())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .employeeCount(employeeCount)
                .customerCount(customerCount)
                .build();
    }
}
