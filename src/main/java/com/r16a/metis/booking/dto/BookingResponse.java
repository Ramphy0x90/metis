package com.r16a.metis.booking.dto;

import com.r16a.metis.booking.models.Booking;
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
public class BookingResponse {
    private UUID id;
    private UUID tenantId;
    private UUID employeeId;
    private UUID serviceId;
    private String employeeName;
    private String serviceName;
    private String clientName;
    private String clientEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Booking.BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
