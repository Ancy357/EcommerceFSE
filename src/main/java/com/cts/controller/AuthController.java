package com.cts.controller;

import com.cts.client.UserServiceClient;
import com.cts.dto.LoginRequest;
import com.cts.dto.LoginResponse;
import com.cts.dto.RegisterRequest;
import com.cts.dto.RegisterResponse;
import com.cts.dto.UserAuthDetailsDto;
import com.cts.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// NEW Import for Feign Exception handling
import feign.FeignException;
// NEW Import for JSON parsing from error body
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map; // To represent the parsed JSON error body

//@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth") // Base path for auth endpoints
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserServiceClient userServiceClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // NEW: ObjectMapper instance for parsing Feign error responses
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Helper method to parse a concatenated validation error string into a Map.
     * Expected format: "field1: error message 1; field2: error message 2"
     * @param concatenatedErrors The error message string from the User Microservice.
     * @return A map where keys are field names and values are error messages.
     */
    private Map<String, String> parseValidationErrors(String concatenatedErrors) {
        Map<String, String> fieldErrors = new HashMap<>();
        if (concatenatedErrors != null && !concatenatedErrors.isEmpty()) {
            String[] errors = concatenatedErrors.split("; ");
            for (String error : errors) {
                String[] parts = error.split(": ", 2); // Split into at most 2 parts (field: message)
                if (parts.length == 2) {
                    fieldErrors.put(parts[0].trim(), parts[1].trim());
                } else {
                    // Handle cases where a message might not have a field prefix
                    logger.warn("Auth Service: Could not parse validation error part: {}", error);
                }
            }
        }
        return fieldErrors;
    }


    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Auth Service: Received registration request for email: {}", request.getEmail());
        try {
            // Call the User Microservice to handle user registration
            ResponseEntity<RegisterResponse> userRegistrationResponse = userServiceClient.registerUser(request);

            if (userRegistrationResponse.getStatusCode().is2xxSuccessful()) {
                logger.info("Auth Service: User registered successfully via User Service for email: {}", request.getEmail());
                return ResponseEntity.ok(userRegistrationResponse.getBody());
            } else {
                logger.warn("Auth Service: User registration failed in User Service for email: {}. Status: {}",
                        request.getEmail(), userRegistrationResponse.getStatusCode());
                // Propagate error from User Service or map to a generic error
                // This original 'else' block would handle cases where User Service returns non-2xx but not a FeignException
                // which is unlikely for validation, but kept for robustness.
                return ResponseEntity.status(userRegistrationResponse.getStatusCode())
                        .body(new RegisterResponse("Registration failed", request.getEmail()));
            }
        // NEW: Catch FeignException.BadRequest specifically
        } catch (FeignException.BadRequest ex) {
            logger.error("Auth Service: Caught Feign BadRequest from User Service for email {}: {}", request.getEmail(), ex.getMessage());
            try {
                // Attempt to parse the original error body from the Feign exception
                // FeignException.contentUTF8() gives you the raw JSON string from the downstream service
                String errorContent = ex.contentUTF8();
                Map<String, Object> errorBody = objectMapper.readValue(errorContent, Map.class);

                String errorMessageFromUserMicroservice = (String) errorBody.getOrDefault("message", "Validation error from user service.");

                // NEW: Parse the concatenated error message into a map
                Map<String, String> parsedFieldErrors = parseValidationErrors(errorMessageFromUserMicroservice);

                // Frontend expects a 'message' field primarily for general errors,
                // and 'fieldErrors' for specific field validation issues.
                // We'll return the parsed fieldErrors and a general message.
                // IMPORTANT: Ensure your RegisterResponse DTO has a `Map<String, String> fieldErrors` field.
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(new RegisterResponse(
                                         "Registration failed. Please check the highlighted fields.", // General message for frontend
                                         request.getEmail(), // Keep email if needed for frontend display
                                         parsedFieldErrors // Pass the parsed field-specific errors
                                     ));
            } catch (Exception parseException) {
                logger.error("Auth Service: Failed to parse Feign error body for email {}: {}", request.getEmail(), parseException.getMessage());
                // Fallback if parsing the error body fails
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new RegisterResponse("A validation error occurred. Please check your input.", request.getEmail()));
            }
        // NEW: Catch other FeignExceptions (e.g., 401, 403, 404, 5xx from downstream)
        } catch (FeignException ex) {
            logger.error("Auth Service: Caught generic Feign Exception from User Service for email {}: {} with status {}",
                         request.getEmail(), ex.getMessage(), ex.status());
            // Attempt to parse the content to see if it's a structured error from downstream
            String propagatedMessage = ex.getMessage(); // Default to Feign's message
            try {
                Map<String, Object> errorBody = objectMapper.readValue(ex.contentUTF8(), Map.class);
                if (errorBody.containsKey("message")) {
                    propagatedMessage = (String) errorBody.get("message");
                }
            } catch (Exception parseException) {
                logger.warn("Auth Service: Could not parse Feign error content for status {}: {}", ex.status(), parseException.getMessage());
            }

            // Re-propagate the original HTTP status from the downstream service
            // IMPORTANT: Ensure your RegisterResponse and LoginResponse DTOs can carry generic error messages.
            return ResponseEntity.status(HttpStatus.valueOf(ex.status()))
                                 .body(new RegisterResponse(
                                     propagatedMessage,
                                     request.getEmail()
                                 ));
        // Original generic exception handler for any other unexpected errors in Auth Service
        } catch (Exception e) {
            logger.error("Auth Service: Error during registration for email {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RegisterResponse("An unexpected error occurred during registration.", request.getEmail()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Auth Service: Received login request for email: {}", request.getEmail());
        try {
            // 1. Call User Microservice to get user details (hashed password, roles, userId)
            ResponseEntity<UserAuthDetailsDto> userDetailsResponse = userServiceClient.getUserAuthDetailsByEmail(request.getEmail());

            if (userDetailsResponse.getStatusCode() != HttpStatus.OK || userDetailsResponse.getBody() == null) {
                logger.warn("Auth Service: User details not found for login email: {}. Status: {}",
                        request.getEmail(), userDetailsResponse.getStatusCode());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(null, request.getEmail(), null, 0)); // Or custom error response
            }

            UserAuthDetailsDto userAuthDetails = userDetailsResponse.getBody();

            // 2. Verify password using the PasswordEncoder (same one used in User Service)
            if (!passwordEncoder.matches(request.getPassword(), userAuthDetails.getPassword())) {
                logger.warn("Auth Service: Invalid credentials for email: {}", request.getEmail());
                // Optionally, inform User Service about failed login attempt here if user service does not track on its own logic
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(null, request.getEmail(), null, 0, "Invalid email or password.")); // Added a user-friendly message
            }

            // 3. If credentials are valid, generate JWT
            String token = jwtUtil.generateToken(userAuthDetails.getUserId(), userAuthDetails.getEmail(), userAuthDetails.getRoles());
            logger.info("Auth Service: Login successful and JWT generated for email: {}", request.getEmail());

            // 4. Return JWT to the client
            return ResponseEntity.ok(new LoginResponse(token, userAuthDetails.getUsername(), userAuthDetails.getRoles(), userAuthDetails.getUserId()));

        // NEW: Similar error handling for login if user microservice sends specific errors
        } catch (FeignException ex) {
            logger.error("Auth Service: Caught Feign Exception during login for email {}: {} with status {}",
                         request.getEmail(), ex.getMessage(), ex.status());
            // Attempt to parse the content to see if it's a structured error
            String errorMessage = "Login failed. Please try again."; // Generic fallback
            try {
                Map<String, Object> errorBody = objectMapper.readValue(ex.contentUTF8(), Map.class);
                if (errorBody.containsKey("message")) {
                    errorMessage = (String) errorBody.get("message");
                }
            } catch (Exception parseException) {
                logger.warn("Auth Service: Could not parse Feign error content for login: {}", parseException.getMessage());
            }
            // IMPORTANT: Ensure LoginResponse can take an error message in its constructor
            return ResponseEntity.status(HttpStatus.valueOf(ex.status()))
                                 .body(new LoginResponse(null, request.getEmail(), null, 0, errorMessage));
        } catch (Exception e) {
            logger.error("Auth Service: Error during login for email {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(null, request.getEmail(), null, 0, "An unexpected error occurred during login.")); // Added a user-friendly message
        }
    }
}
