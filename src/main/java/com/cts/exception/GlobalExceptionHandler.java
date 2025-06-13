package com.cts.exception;

import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.Map;
import java.util.stream.Collectors; // For more concise validation error logging

@ControllerAdvice
public class GlobalExceptionHandler {

    // Initialize the logger for this class
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation errors thrown by @Valid annotation on DTOs.
     * Returns a 400 Bad Request with a map of field errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getAllErrors().stream()
                .filter(error -> error instanceof FieldError) // Ensure we only process FieldError
                .collect(Collectors.toMap(
                        error -> ((FieldError) error).getField(),
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Validation error"
                ));

        logger.warn("GlobalExceptionHandler: Validation error occurred. Errors: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles UserNotFoundException, returning a 404 Not Found status.
     */
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        logger.warn("GlobalExceptionHandler: User not found. Message: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Handles AddressNotFoundException, returning a 404 Not Found status.
     */
    @ExceptionHandler(AddressNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleAddressNotFoundException(AddressNotFoundException ex) {
        logger.warn("GlobalExceptionHandler: Address not found. Message: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Handles EmailAlreadyExistsException, returning a 409 Conflict status.
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<String> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        logger.warn("GlobalExceptionHandler: Email already exists. Message: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    /**
     * Generic handler for IllegalArgumentException, often used for business logic errors.
     * Returns a 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("GlobalExceptionHandler: Invalid argument. Message: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Catch-all for any other RuntimeExceptions that might be thrown from the service layer.
     * This provides a consistent way to handle unexpected business logic failures.
     * Consider categorizing more specific RuntimeExceptions if needed.
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        // Log the full exception for debugging purposes, as these are often unexpected errors
        logger.error("GlobalExceptionHandler: Unhandled RuntimeException occurred. Message: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + ex.getMessage());
    }

    /**
     * Fallback handler for any other unhandled Exceptions, ensuring no unhandled exceptions
     * leak internal server details.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        // Log the full exception for debugging
        logger.error("GlobalExceptionHandler: Catch-all Exception occurred. Message: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal server error occurred.");
    }
}