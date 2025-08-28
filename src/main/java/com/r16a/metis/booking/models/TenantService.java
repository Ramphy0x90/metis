package com.r16a.metis.booking.models;

import com.r16a.metis.identity.models.Tenant;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name="services")
public class TenantService {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private int durationMinutes;
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "tenant_uuid")
    private Tenant tenant;
}
