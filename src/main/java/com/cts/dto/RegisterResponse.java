package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map; // Don't forget this import for Map

@Data
// @AllArgsConstructor // Lombok's @AllArgsConstructor might conflict with custom constructors,
                      // or create one that matches all fields including fieldErrors.
                      // If you use custom constructors, you might remove this.
@NoArgsConstructor
public class RegisterResponse {
    private String message;
    private String email;
    // NEW: Field to hold specific validation errors from the backend
    private Map<String, String> fieldErrors;

    // Existing constructor (for success or general messages without field errors)
    public RegisterResponse(String message, String email) {
        this.message = message;
        this.email = email;
        this.fieldErrors = null; // Initialize to null or an empty map
    }

    // NEW: Constructor to include field-specific errors
    public RegisterResponse(String message, String email, Map<String, String> fieldErrors) {
        this.message = message;
        this.email = email;
        this.fieldErrors = fieldErrors;
    }
}
