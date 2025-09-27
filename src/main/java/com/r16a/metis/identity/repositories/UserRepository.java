package com.r16a.metis.identity.repositories;

import com.r16a.metis.identity.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    List<User> findByTenantId(UUID tenantId);
    
    @Modifying
    @Query("DELETE FROM User u WHERE u.tenant.id = :tenantId")
    void deleteByTenantId(@Param("tenantId") UUID tenantId);
}
