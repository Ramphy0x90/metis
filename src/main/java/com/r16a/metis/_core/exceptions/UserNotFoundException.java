package com.r16a.metis._core.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String userEmail) {
        super("User not found: " + userEmail);
    }
}
