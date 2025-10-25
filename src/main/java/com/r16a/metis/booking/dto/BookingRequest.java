package com.r16a.metis.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {
    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;
    
    @NotNull(message = "Service ID is required")
    private UUID serviceId;
    
    @NotNull(message = "Employee ID is required")
    private UUID employeeId;
    
    @NotBlank(message = "Client name is required")
    private String clientName;
    
    @NotBlank(message = "Client email is required")
    @Email(message = "Email must be valid")
    private String clientEmail;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
}
