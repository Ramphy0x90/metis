package com.r16a.metis.booking.repositories;

import com.r16a.metis.booking.models.Booking;
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
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    
    Page<Booking> findByTenantId(UUID tenantId, Pageable pageable);

    List<Booking> findByTenantId(UUID tenantId);

    Page<Booking> findByEmployeeId(UUID employeeId, Pageable pageable);
    
    List<Booking> findByServiceId(UUID serviceId);
    
    @Modifying
    @Query("DELETE FROM Booking b WHERE b.tenant.id = :tenantId")
    void deleteByTenantId(@Param("tenantId") UUID tenantId);
    
    @Modifying
    @Query("DELETE FROM Booking b WHERE b.employee.id = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") UUID employeeId);
    
    @Modifying
    @Query("DELETE FROM Booking b WHERE b.service.id = :serviceId")
    void deleteByServiceId(@Param("serviceId") UUID serviceId);
}
