package com.r16a.metis.identity.dto;

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
public class TenantResponse {
    private UUID id;
    private String name;
    private String domain;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long employeeCount;
    private long customerCount;
}
