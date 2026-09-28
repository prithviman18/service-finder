package com.project.util;

/**
 * Custom runtime exception for validation failures across the application.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
