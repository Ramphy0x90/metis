package com.r16a.metis.booking.controllers;

import com.r16a.metis.booking.dto.BookingRequest;
import com.r16a.metis.booking.dto.BookingResponse;
import com.r16a.metis.booking.dto.BookingUpdateRequest;
import com.r16a.metis.booking.models.Booking;
import com.r16a.metis.booking.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    
    @GetMapping
    public ResponseEntity<Page<BookingResponse>> getAllBookings(Pageable pageable) {
        Page<Booking> bookings = bookingService.getAllBookings(pageable);
        Page<BookingResponse> response = bookings.map(this::mapToResponse);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable UUID id) {
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(mapToResponse(booking));
    }
    
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        Booking booking = bookingService.createBooking(
            request.getTenantId(),
            request.getServiceId(),
            request.getEmployeeId(),
            request.getClientName(),
            request.getClientEmail(),
            request.getStartTime(),
            request.getEndTime()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(booking));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(
            @PathVariable UUID id,
            @Valid @RequestBody BookingUpdateRequest request
    ) {
        Booking booking = bookingService.updateBooking(id, request);
        return ResponseEntity.ok(mapToResponse(booking));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable UUID id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<Page<BookingResponse>> getBookingsByTenant(
            @PathVariable UUID tenantId,
            Pageable pageable
    ) {
        Page<Booking> bookings = bookingService.getBookingsByTenant(tenantId, pageable);
        Page<BookingResponse> response = bookings.map(this::mapToResponse);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<BookingResponse>> getBookingsByEmployee(
            @PathVariable UUID employeeId,
            Pageable pageable
    ) {
        Page<Booking> bookings = bookingService.getBookingsByEmployee(employeeId, pageable);
        Page<BookingResponse> response = bookings.map(this::mapToResponse);
        return ResponseEntity.ok(response);
    }
    
    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .tenantId(booking.getTenant() != null ? booking.getTenant().getId() : null)
                .employeeId(booking.getEmployee() != null ? booking.getEmployee().getId() : null)
                .serviceId(booking.getService() != null ? booking.getService().getId() : null)
                .employeeName(booking.getEmployee() != null ? 
                    (booking.getEmployee().getName() + " " + booking.getEmployee().getSurname()) : null)
                .serviceName(booking.getService() != null ? booking.getService().getTitle() : null)
                .clientName(booking.getClientName())
                .clientEmail(booking.getClientEmail())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
