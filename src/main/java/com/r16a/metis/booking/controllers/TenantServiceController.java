package com.r16a.metis.booking.controllers;

import com.r16a.metis.booking.dto.TenantServiceRequest;
import com.r16a.metis.booking.dto.TenantServiceResponse;
import com.r16a.metis.booking.models.TenantService;
import com.r16a.metis.booking.services.TenantServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class TenantServiceController {
    private final TenantServiceService tenantServiceService;
    
    @GetMapping
    public ResponseEntity<Page<TenantServiceResponse>> getAllServices(Pageable pageable) {
        Page<TenantService> services = tenantServiceService.getAllServices(pageable);
        Page<TenantServiceResponse> response = services.map(this::mapToResponse);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TenantServiceResponse> getServiceById(@PathVariable UUID id) {
        TenantService service = tenantServiceService.getServiceById(id);
        return ResponseEntity.ok(mapToResponse(service));
    }
    
    @PostMapping
    public ResponseEntity<TenantServiceResponse> createService(
            @Valid @RequestBody TenantServiceRequest request
    ) {
        TenantService service = tenantServiceService.createService(
            request.getTenantId(),
            request.getTitle(),
            request.getDescription(),
            request.getDurationMinutes(),
            request.getPrice()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(service));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TenantServiceResponse> updateService(
            @PathVariable UUID id,
            @Valid @RequestBody TenantServiceRequest request
    ) {
        TenantService service = tenantServiceService.updateService(
            id,
            request.getTitle(),
            request.getDescription(),
            request.getDurationMinutes(),
            request.getPrice()
        );
        return ResponseEntity.ok(mapToResponse(service));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable UUID id) {
        tenantServiceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<Page<TenantServiceResponse>> getServicesByTenant(
            @PathVariable UUID tenantId,
            Pageable pageable
    ) {
        Page<TenantService> services = tenantServiceService.getServicesByTenant(tenantId, pageable);
        Page<TenantServiceResponse> response = services.map(this::mapToResponse);
        return ResponseEntity.ok(response);
    }
    
    private TenantServiceResponse mapToResponse(TenantService service) {
        return TenantServiceResponse.builder()
                .id(service.getId())
                .tenantId(service.getTenant() != null ? service.getTenant().getId() : null)
                .tenantName(service.getTenant() != null ? service.getTenant().getName() : null)
                .title(service.getTitle())
                .description(service.getDescription())
                .durationMinutes(service.getDurationMinutes())
                .price(service.getPrice())
                .build();
    }
}
