package com.cts.dto;

import java.util.List;
import lombok.AllArgsConstructor; // Keep if you want Lombok to generate an all-args constructor for all fields
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Required for JSON deserialization
// @AllArgsConstructor // You might remove this if you prefer specific constructors,
                       // or ensure it generates one that includes all fields (token, email, roles, userId, message)
public class LoginResponse {
    private String token;
    private String email;
    private List<String> roles;
    private int userId;
    // NEW: Field to hold a general error/success message
    private String message;

    // Existing constructor (assuming successful login)
    public LoginResponse(String token, String email, List<String> roles, int userId) {
        this.token = token;
        this.email = email;
        this.roles = roles;
        this.userId = userId;
        this.message = "Login successful!"; // Default success message or null
    }

    // NEW: Constructor specifically for error cases or when only a message is needed
    // This allows the AuthController to pass error messages directly.
    public LoginResponse(String token, String email, List<String> roles, int userId, String message) {
        this.token = token;
        this.email = email;
        this.roles = roles;
        this.userId = userId;
        this.message = message;
    }

    // If you always want to include a message, you might also update the first constructor:
    // public LoginResponse(String token, String email, List<String> roles, int userId) {
    //     this(token, email, roles, userId, "Login successful!");
    // }
}
