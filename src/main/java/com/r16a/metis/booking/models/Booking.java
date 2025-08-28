package com.r16a.metis.booking.models;

import com.r16a.metis.identity.models.Tenant;
import com.r16a.metis.identity.models.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name="bookings")
public class Booking {
    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "tenant_uuid")
    private Tenant tenant;

    @ManyToOne
    @JoinColumn(name = "employee_uuid")
    private User employee;

    @ManyToOne
    @JoinColumn(name = "service_uuid")
    private TenantService service;

    private String clientName;
    private String clientEmail;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
