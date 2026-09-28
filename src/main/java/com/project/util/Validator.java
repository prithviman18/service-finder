package com.project.util;

import java.util.regex.Pattern;

/**
 * Utility class providing static validation methods.
 * Centralizes all validation logic for Aadhar, Phone, Location, etc.
 */
public final class Validator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");
    private static final Pattern AADHAR_PATTERN = Pattern.compile("^\\d{12}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    private Validator() {
        // Private constructor for static utility class
    }

    public static void validateUsername(String username) {
        if (username == null || !USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new ValidationException("Username must be 3-20 characters long and contain only letters, numbers, and underscores.");
        }
    }

    public static void validatePassword(String password) {
        if (password == null || password.trim().length() < 4) {
            throw new ValidationException("Password must be at least 4 characters long.");
        }
    }

    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Name is mandatory and cannot be blank.");
        }
    }

    public static void validatePhone(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new ValidationException("Phone number must be exactly 10 digits.");
        }
    }

    public static void validateAadhar(String aadhar) {
        if (aadhar == null || !AADHAR_PATTERN.matcher(aadhar.trim()).matches()) {
            throw new ValidationException("Aadhar number must be exactly 12 digits.");
        }
    }

    public static void validateLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new ValidationException("Location is mandatory and cannot be blank.");
        }
    }
}
