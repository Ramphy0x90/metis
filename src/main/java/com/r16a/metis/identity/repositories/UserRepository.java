package com.r16a.metis.identity.repositories;

import com.r16a.metis.identity.models.User;
import com.r16a.metis.identity.models.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    List<User> findByTenantId(UUID tenantId);

    Page<User> findByTenantId(UUID tenantId, Pageable pageable);
    
    @Modifying
    @Query("DELETE FROM User u WHERE u.tenant.id = :tenantId")
    void deleteByTenantId(@Param("tenantId") UUID tenantId);
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE u.tenant.id = :tenantId AND r.name = :userRole")
    long countUserOfTypeByTenantId(@Param("tenantId") UUID tenantId, @Param("userRole") UserRole userRole);
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :userRole")
    long countTotalUsersByType(@Param("userRole") UserRole userRole);
}
