package com.cts.controller;

import com.cts.dto.*;
import com.cts.service.IUserService;
import jakarta.validation.Valid; // Import the @Valid annotation
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/users")
@EnableFeignClients(basePackages = "com.cts.client")
@RequiredArgsConstructor
public class UserController {

    // Initialize the logger for this class
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final IUserService userService;

    // Registration
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("API Request: Registering new user with email: {}", request.getEmail());
        RegisterResponse response = userService.registerUser(request);
        logger.info("API Response: User registered successfully for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        logger.info("API Request: User login attempt for email: {}", request.getEmail());
        try {
            String response = userService.login(request);
            logger.info("API Response: User login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // This specific catch block for login is appropriate if your service throws RuntimeException
            // specifically for UNAUTHORIZED scenarios like invalid credentials, not just generic 500s.
            logger.warn("API Response: Login failed for email: {}. Reason: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // Get user by ID
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUser(@PathVariable int userId) {
        logger.info("API Request: Fetching user profile for ID: {}", userId);
        UserProfileResponse response = userService.getUserById(userId);
        logger.info("API Response: User profile fetched successfully for ID: {}", userId);
        return ResponseEntity.ok(response);
    }

    // Get all users
    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers() {
        logger.info("API Request: Fetching all users.");
        List<UserSummaryResponse> users = userService.getAllUsers();
        logger.info("API Response: Fetched {} users.", users.size());
        return ResponseEntity.ok(users);
    }

    // Update profile
    @PutMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable int userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        logger.info("API Request: Updating profile for user ID: {}", userId);
        UserProfileResponse response = userService.updateUserProfile(userId, request);
        logger.info("API Response: Profile updated successfully for user ID: {}", userId);
        return ResponseEntity.ok(response);
    }

    // Change password
    @PutMapping("/{userId}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable int userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        logger.info("API Request: Changing password for user ID: {}", userId);
        userService.changePassword(request); // Assuming service uses email from request for identification
        logger.info("API Response: Password changed successfully for user ID: {}", userId);
        return ResponseEntity.ok().build();
    }

    // Forgot password
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        logger.info("API Request: Forgot password request for email: {}", request.getEmail());
        userService.forgotPassword(request);
        logger.info("API Response: Forgot password process initiated for email: {}", request.getEmail());
        return ResponseEntity.ok().build();
    }

    // Reset password
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        logger.info("API Request: Resetting password for token: {}", request.getToken()); // Be careful with logging tokens in production
        userService.resetPassword(request);
        logger.info("API Response: Password reset successfully.");
        return ResponseEntity.ok().build();
    }

    // Account recovery
    @PostMapping("/recover-account")
    public ResponseEntity<Void> recoverAccount(@Valid @RequestBody AccountRecoveryRequest request) {
        logger.info("API Request: Account recovery request for email: {}", request.getEmail());
        userService.recoverAccount(request);
        logger.info("API Response: Account recovery process initiated for email: {}", request.getEmail());
        return ResponseEntity.ok().build();
    }

    // Get block status
    @GetMapping("/block-status")
    public ResponseEntity<UserBlockStatusResponse> getBlockStatus(@RequestParam String email) {
        logger.info("API Request: Getting block status for email: {}", email);
        UserBlockStatusResponse response = userService.getBlockStatus(email);
        logger.info("API Response: Block status fetched for email: {}", email);
        return ResponseEntity.ok(response);
    }

    // Unlock user
    @PostMapping("/unlock/{email}")
    public ResponseEntity<Void> unlockUser(@PathVariable String email) {
        logger.info("API Request: Unlocking user with email: {}", email);
        userService.unlockUser(email);
        logger.info("API Response: User unlocked successfully for email: {}", email);
        return ResponseEntity.ok().build();
    }

    // Assign roles
    @PutMapping("/assign-roles")
    public ResponseEntity<Void> assignRoles(@Valid @RequestBody RoleAssignmentRequest request) {
        logger.info("API Request: Assigning roles to user ID: {}", request.getUserId());
        userService.assignRoles(request);
        logger.info("API Response: Roles assigned successfully for user ID: {}", request.getUserId());
        return ResponseEntity.ok().build();
    }

    // Update user status
    @PutMapping("/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable int userId,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        logger.info("API Request: Updating status for user ID: {}. New status: {}", userId, request.isActive() ? "Active" : "Inactive");
        userService.updateUserStatus(userId, request);
        logger.info("API Response: User status updated successfully for ID: {}", userId);
        return ResponseEntity.ok().build();
    }

    // Delete user (soft delete)
    @DeleteMapping("/softdelete/{userId}")
    public ResponseEntity<Void> softdeleteUser(@PathVariable int userId) {
        logger.info("API Request: Soft deleting user with ID: {}", userId);
        userService.softdeleteUser(userId);
        logger.info("API Response: User soft deleted successfully for ID: {}", userId);
        return ResponseEntity.ok().build();
    }

    // Delete user permanently (hard delete)
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Void> hardDeleteUser(@PathVariable int userId) {
        logger.warn("API Request: PERMANENTLY deleting user with ID: {}", userId); // Use warn level for irreversible action
        userService.hardDeleteUser(userId);
        logger.info("API Response: User permanently deleted for ID: {}", userId);
        return ResponseEntity.ok().build();
    }

    // Get user ID (seems like a duplicate of /api/users/{userId} but returns only ID)
    @GetMapping("/{userId}/id")
    public ResponseEntity<Integer> getUserId(@PathVariable int userId){
        logger.info("API Request: Fetching user ID (specific endpoint) for ID: {}", userId);
        int id = userService.getUserId(userId); // Assuming getUserId is now in your IUserService
        logger.info("API Response: User ID {} retrieved successfully.", id);
        return ResponseEntity.ok(id);
    }

    // Get cart items for a user
    @GetMapping("/{userId}/viewAllProducts")
    public ResponseEntity<List<CartItemDTO>> getCartItems(@PathVariable Integer userId) {
        logger.info("API Request: Fetching cart items for user ID: {}", userId);
        List<CartItemDTO> cartItems = userService.getUserCartItems(userId);
        logger.info("API Response: Fetched {} cart items for user ID: {}", cartItems.size(), userId);
        return ResponseEntity.ok(cartItems);
        

    }
    
    @GetMapping("/auth/{email}") // Changed path to use email
    public ResponseEntity<UserAuthDetailsDto> getUserAuthDetailsByEmail(@PathVariable String email) { // Changed parameter
        logger.info("Received authentication details request for email: {}", email);
        try {
            UserAuthDetailsDto userAuthDetails = userService.getUserAuthDetailsByEmail(email);

            if (userAuthDetails == null) { // Service layer returns null if user not found
                logger.warn("Authentication details not found for email: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok(userAuthDetails);
        } catch (Exception e) {
            logger.error("Error fetching authentication details for email {}: {}", email, e.getMessage(), e);
            // Consider more specific error handling/logging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}