package com.r16a.metis._core.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String userEmail) {
        super("User with email " + userEmail + " already exists");
    }
}
