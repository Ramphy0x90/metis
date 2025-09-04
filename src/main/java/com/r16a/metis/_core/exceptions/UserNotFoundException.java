package com.r16a.metis._core.exceptions;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID id) {
        super("User with ID not found: " + id);
    }

    public UserNotFoundException(String userEmail) {
        super("User with email not found: " + userEmail);
    }
}
