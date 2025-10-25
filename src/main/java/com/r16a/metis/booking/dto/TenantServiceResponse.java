package com.r16a.metis.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantServiceResponse {
    private UUID id;
    private UUID tenantId;
    private String tenantName;
    private String name;
    private Integer durationMinutes;
    private BigDecimal price;
}
