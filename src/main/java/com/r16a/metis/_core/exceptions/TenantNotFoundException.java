package com.r16a.metis._core.exceptions;

import java.util.UUID;

public class TenantNotFoundException extends RuntimeException {
    public TenantNotFoundException(UUID id) {
        super("Tenant not found with id: " + id);
    }
}
