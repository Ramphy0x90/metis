package com.r16a.metis.booking.services;

import com.r16a.metis._core.audit.AuditService;
import com.r16a.metis._core.exceptions.TenantNotFoundException;
import com.r16a.metis._core.exceptions.UserNotFoundException;
import com.r16a.metis.booking.models.Booking;
import com.r16a.metis.booking.models.TenantService;
import com.r16a.metis.booking.repositories.BookingRepository;
import com.r16a.metis.booking.repositories.TenantServiceRepository;
import com.r16a.metis.identity.models.User;
import com.r16a.metis.identity.repositories.TenantRepository;
import com.r16a.metis.identity.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final TenantServiceRepository tenantServiceRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final AuditService auditService;
    
    public Booking createBooking(UUID tenantId, UUID serviceId, UUID employeeId, 
                                String clientName, String clientEmail, 
                                LocalDateTime startTime, LocalDateTime endTime) {
        
        // Validate tenant exists
        if (!tenantRepository.existsById(tenantId)) {
            throw new TenantNotFoundException(tenantId);
        }
        
        // Validate service exists and belongs to tenant
        TenantService service = tenantServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        if (!service.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Service does not belong to the specified tenant");
        }
        
        // Validate employee exists and belongs to tenant
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new UserNotFoundException(employeeId));
        if (employee.getTenant() == null || !employee.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Employee does not belong to the specified tenant");
        }
        
        Booking booking = new Booking();
        booking.setTenant(service.getTenant());
        booking.setService(service);
        booking.setEmployee(employee);
        booking.setClientName(clientName);
        booking.setClientEmail(clientEmail);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus(Booking.BookingStatus.PENDING);
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // Audit log the creation
        auditService.logCreate("Booking", savedBooking.getId(), savedBooking, tenantId.toString());
        
        log.info("Created booking for client: {} with ID: {}", clientEmail, savedBooking.getId());
        return savedBooking;
    }
    
    public Booking updateBookingStatus(UUID bookingId, Booking.BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Store old values for audit
        Booking oldBooking = new Booking();
        oldBooking.setId(booking.getId());
        oldBooking.setStatus(booking.getStatus());
        oldBooking.setClientName(booking.getClientName());
        oldBooking.setClientEmail(booking.getClientEmail());
        oldBooking.setStartTime(booking.getStartTime());
        oldBooking.setEndTime(booking.getEndTime());
        
        booking.setStatus(status);
        Booking updatedBooking = bookingRepository.save(booking);
        
        // Audit log the update
        String tenantId = updatedBooking.getTenant() != null ? updatedBooking.getTenant().getId().toString() : null;
        auditService.logUpdate("Booking", updatedBooking.getId(), oldBooking, updatedBooking, tenantId);
        
        log.info("Updated booking status to {} for booking ID: {}", status, updatedBooking.getId());
        return updatedBooking;
    }
    
    public void deleteBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        String tenantId = booking.getTenant() != null ? booking.getTenant().getId().toString() : null;
        
        bookingRepository.deleteById(bookingId);
        
        // Audit log the deletion
        auditService.logDelete("Booking", bookingId, booking, tenantId);
        
        log.info("Deleted booking with ID: {}", bookingId);
    }
    
    public List<Booking> getBookingsByTenant(UUID tenantId) {
        return bookingRepository.findByTenantId(tenantId);
    }
    
    public List<Booking> getBookingsByEmployee(UUID employeeId) {
        return bookingRepository.findByEmployeeId(employeeId);
    }
}
