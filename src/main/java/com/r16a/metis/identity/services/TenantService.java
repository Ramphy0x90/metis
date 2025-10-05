package com.r16a.metis.identity.services;

import com.r16a.metis._core.audit.AuditService;
import com.r16a.metis._core.exceptions.TenantNotFoundException;
import com.r16a.metis.booking.repositories.BookingRepository;
import com.r16a.metis.booking.repositories.TenantServiceRepository;
import com.r16a.metis.identity.dto.TenantCreateRequest;
import com.r16a.metis.identity.dto.TenantResponse;
import com.r16a.metis.identity.dto.TenantUpdateRequest;
import com.r16a.metis.identity.models.Tenant;
import com.r16a.metis.identity.models.UserRole;
import com.r16a.metis.identity.repositories.TenantRepository;
import com.r16a.metis.identity.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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

    public Page<TenantResponse> getAllTenants(Pageable pageable) {
        return searchTenants(null, pageable);
    }

    public Page<TenantResponse> searchTenants(@Nullable String q, Pageable pageable) {
        Specification<Tenant> spec = Specification.unrestricted();

        if (q != null && !q.isBlank()) {
            String trimmed = q.trim();
            UUID idCandidate = null;
            try {
                idCandidate = UUID.fromString(trimmed);
            } catch (IllegalArgumentException ignored) {
            }

            final UUID finalIdCandidate = idCandidate;
            String like = "%" + trimmed.toLowerCase() + "%";
            Specification<Tenant> idSpec = (root, query, cb) -> finalIdCandidate != null ? cb.equal(root.get("id"), finalIdCandidate) : null;
            Specification<Tenant> nameSpec = (root, query, cb) -> cb.like(cb.lower(root.get("name")), like);
            Specification<Tenant> domainSpec = (root, query, cb) -> cb.like(cb.lower(root.get("domain")), like);

            spec = spec.and(idSpec.or(nameSpec).or(domainSpec));
        }

        Page<Tenant> tenantsPage = tenantRepository.findAll(spec, pageable);
        List<TenantResponse> content = tenantsPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return new PageImpl<>(content, pageable, tenantsPage.getTotalElements());
    }

    public TenantResponse getTenantById(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException(id));
        return mapToResponse(tenant);
    }

    public TenantResponse getTenantByDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("Domain must be provided");
        }

        String normalized = domain.trim();
        Tenant tenant = tenantRepository.findByDomainIgnoreCase(normalized)
                .orElseThrow(() -> new TenantNotFoundException(normalized));
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
        long adminCount = userRepository.countUserOfTypeByTenantId(tenant.getId(), UserRole.ADMIN);
        long employeeCount = userRepository.countUserOfTypeByTenantId(tenant.getId(), UserRole.EMPLOYEE);
        long customerCount = userRepository.countUserOfTypeByTenantId(tenant.getId(), UserRole.USER);

        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .domain(tenant.getDomain())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .adminCount(adminCount)
                .employeeCount(employeeCount)
                .customerCount(customerCount)
                .build();
    }
}
