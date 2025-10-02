package com.r16a.metis.identity.controllers;

import com.r16a.metis.identity.dto.AllTenantResponse;
import com.r16a.metis.identity.dto.TenantCreateRequest;
import com.r16a.metis.identity.dto.TenantResponse;
import com.r16a.metis.identity.dto.TenantUpdateRequest;
import com.r16a.metis.identity.services.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {
    private final TenantService tenantService;
    
    @GetMapping
    public ResponseEntity<Page<TenantResponse>> getAllTenants(Pageable pageable) {
        Page<TenantResponse> tenants = tenantService.getAllTenants(pageable);
        return ResponseEntity.ok(tenants);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable UUID id) {
        TenantResponse tenant = tenantService.getTenantById(id);
        return ResponseEntity.ok(tenant);
    }
    
    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody TenantCreateRequest request) {
        TenantResponse createdTenant = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTenant);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TenantResponse> updateTenant(
            @PathVariable UUID id,
            @Valid @RequestBody TenantUpdateRequest request
    ) {
        TenantResponse updatedTenant = tenantService.updateTenant(id, request);
        return ResponseEntity.ok(updatedTenant);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable UUID id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }
}
