package com.r16a.metis.identity.repositories;

import com.r16a.metis.identity.models.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID>, JpaSpecificationExecutor<Tenant> {
}
