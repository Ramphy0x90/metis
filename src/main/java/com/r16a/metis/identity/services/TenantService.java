package com.r16a.metis.identity.services;

import com.r16a.metis._core.exceptions.TenantNotFoundException;
import com.r16a.metis.identity.dto.TenantCreateRequest;
import com.r16a.metis.identity.dto.TenantResponse;
import com.r16a.metis.identity.dto.TenantUpdateRequest;
import com.r16a.metis.identity.models.Tenant;
import com.r16a.metis.identity.repositories.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TenantService {
    private final TenantRepository tenantRepository;
    
    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(this::mapToResponse)
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
        return mapToResponse(savedTenant);
    }
    
    public TenantResponse updateTenant(UUID id, TenantUpdateRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException(id));
        
        tenant.setName(request.getName());
        tenant.setDomain(request.getDomain());
        
        Tenant updatedTenant = tenantRepository.save(tenant);
        return mapToResponse(updatedTenant);
    }
    
    public void deleteTenant(UUID id) {
        if (!tenantRepository.existsById(id)) {
            throw new TenantNotFoundException(id);
        }
        
        tenantRepository.deleteById(id);
    }
    
    private TenantResponse mapToResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .domain(tenant.getDomain())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
