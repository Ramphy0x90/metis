package com.r16a.metis.booking.repositories;

import com.r16a.metis.booking.models.TenantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TenantServiceRepository extends JpaRepository<TenantService, UUID> {
    
    Page<TenantService> findByTenantId(UUID tenantId, Pageable pageable);

    List<TenantService> findByTenantId(UUID tenantId);

    @Modifying
    @Query("DELETE FROM TenantService ts WHERE ts.tenant.id = :tenantId")
    void deleteByTenantId(@Param("tenantId") UUID tenantId);
}
