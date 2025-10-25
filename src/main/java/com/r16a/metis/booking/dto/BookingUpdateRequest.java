package com.r16a.metis.booking.dto;

import com.r16a.metis.booking.models.Booking;
import jakarta.validation.constraints.Email;
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
public class BookingUpdateRequest {
    private UUID serviceId;
    private UUID employeeId;
    private String clientName;
    
    @Email(message = "Email must be valid")
    private String clientEmail;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Booking.BookingStatus status;
}
