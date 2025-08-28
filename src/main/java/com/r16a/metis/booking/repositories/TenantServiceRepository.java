package com.r16a.metis.booking.repositories;

import com.r16a.metis.booking.models.TenantService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TenantServiceRepository extends JpaRepository<TenantService, UUID> {
}
