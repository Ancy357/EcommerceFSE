
package com.cts.controller;

import com.cts.dto.*;
import com.cts.service.IUserService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
	
    private final IUserService userService;

    // Registration
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        try {
            String response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
    //login is handled in apigateway- requests to user microservice endpoints here are routed only after authentication at apigateway

    // Get user by ID
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUser(@PathVariable int userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // Get all users
    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Update profile
    @PutMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> updateProfile(@PathVariable int userId, @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateUserProfile(userId, request));
    }

    // Change password
    @PutMapping("/{userId}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable int userId, @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    // Forgot password
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }

    // Reset password
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    // Account recovery
    @PostMapping("/recover-account")
    public ResponseEntity<Void> recoverAccount(@RequestBody AccountRecoveryRequest request) {
        userService.recoverAccount(request);
        return ResponseEntity.ok().build();
    }

    // Get block status
    @GetMapping("/block-status")
    public ResponseEntity<UserBlockStatusResponse> getBlockStatus(@RequestParam String email) {
        return ResponseEntity.ok(userService.getBlockStatus(email));
    }

    // Unlock user
    @PostMapping("/unlock/{email}")
    public ResponseEntity<Void> unlockUser(@PathVariable String email) {
        userService.unlockUser(email);
        return ResponseEntity.ok().build();
    }

    // Assign roles
    @PutMapping("/assign-roles")
    public ResponseEntity<Void> assignRoles(@RequestBody RoleAssignmentRequest request) {
        userService.assignRoles(request);
        return ResponseEntity.ok().build();
    }

    // Update user status
    @PutMapping("/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable int userId, @RequestBody UserStatusUpdateRequest request) {
        userService.updateUserStatus(userId, request);
        return ResponseEntity.ok().build();
    }

    // Delete user (soft delete)
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable int userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
}
