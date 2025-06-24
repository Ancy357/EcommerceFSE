package com.cts.service; // Your existing package

import com.cts.dto.*;
import com.cts.entity.User;
import com.cts.enums.Role;
import com.cts.exception.EmailAlreadyExistsException;
import com.cts.exception.UserNotFoundException;
import com.cts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cts.client.CartServiceClient;
import com.cts.client.OrderServiceClient;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    private final ModelMapper modelMapper;

    private final CartServiceClient cartServiceClient;
    
    private final OrderServiceClient orderServiceClient;

    // Existing methods (login, registerUser, getUserById, etc.) ...
    @Override
    public String login(LoginRequest request) {
        logger.info("Attempting login for user with email: {}", request.getEmail());
        User user;
        try {
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            logger.debug("User found for email: {}", request.getEmail());
        } catch (UserNotFoundException e) {
            logger.warn("Login attempt failed for non-existent email: {}. Error: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while finding user by email {}: {}", request.getEmail(), e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred during login.", e);
        }

        if (user.isBlocked() && user.getBlockedUntil().isAfter(LocalDateTime.now())) {
            logger.warn("Login attempt for blocked user: {}. Blocked until: {}", user.getEmail(), user.getBlockedUntil());
            throw new RuntimeException("User is blocked until " + user.getBlockedUntil());
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Invalid password for user: {}. Handling failed login.", user.getEmail());
            handleFailedLogin(user.getEmail());
            throw new RuntimeException("Invalid credentials");
        }

        try {
            user.setLoginAttempts(0);
            userRepository.save(user);
            logger.info("User {} logged in successfully. Login attempts reset.", user.getEmail());
        } catch (Exception e) {
            logger.error("Error resetting login attempts for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Error during login, failed to reset attempts.", e);
        }

        return "Login successful";
    }

    @Override
    public RegisterResponse registerUser(RegisterRequest request) {
        logger.info("Attempting to register new user with email: {}", request.getEmail());
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                logger.warn("User registration failed: Email already registered - {}", request.getEmail());
                throw new EmailAlreadyExistsException("Email already registered");
            }
            User user = modelMapper.map(request, User.class);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setRoles(Set.of(Role.USER));

            User savedUser = userRepository.save(user);
            logger.info("User registered successfully with email: {}. User ID: {}", savedUser.getEmail(), savedUser.getUserID());

            try {
                logger.info("Attempting to create cart for new user with ID: {}", savedUser.getUserID());
                ResponseEntity<String> cartCreationResponse = cartServiceClient.createCart(savedUser.getUserID());

                if (cartCreationResponse.getStatusCode().is2xxSuccessful()) {
                    logger.info("Cart created successfully for user ID {}. Response from Cart Service: {}",
                                 savedUser.getUserID(), cartCreationResponse.getBody());
                } else {
                    logger.warn("Failed to create cart for user ID {} from Cart Service. Status: {}, Body: {}",
                                 savedUser.getUserID(), cartCreationResponse.getStatusCode(), cartCreationResponse.getBody());
                    throw new RuntimeException("Failed to create cart for new user. Cart Service responded with status: " + cartCreationResponse.getStatusCode());
                }
            } catch (feign.FeignException.FeignClientException e) {
                logger.error("Client error from Cart Service when creating cart for user ID {}: Status: {}, Message: {}",
                             savedUser.getUserID(), e.status(), e.contentUTF8(), e);
                throw new RuntimeException("Error from Cart Service (client error): " + e.getMessage(), e);
            } catch (feign.FeignException.FeignServerException e) {
                logger.error("Server error from Cart Service when creating cart for user ID {}: Status: {}, Message: {}",
                             savedUser.getUserID(), e.status(), e.contentUTF8(), e);
                throw new RuntimeException("Cart Service internal error (server error): " + e.getMessage(), e);
            } catch (Exception e) {
                logger.error("Unexpected error communicating with Cart Service to create cart for user ID {}: {}",
                             savedUser.getUserID(), e.getMessage(), e);
                throw new RuntimeException("Error communicating with Cart Service during cart creation.", e);
            }

            return new RegisterResponse("User registered successfully", savedUser.getEmail());

        } catch (EmailAlreadyExistsException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred during user registration for email {}: {}", request.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to register user.", e);
        }
    }

    @Override
    public UserProfileResponse getUserById(int userId) {
        logger.debug("Attempting to fetch user with ID: {}", userId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            logger.info("User found for ID: {}. Email: {}", userId, user.getEmail());
            return modelMapper.map(user, UserProfileResponse.class);
        } catch (UserNotFoundException e) {
            logger.warn("User fetch failed: User with ID {} not found. Error: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while fetching user by ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch user profile.", e);
        }
    }

    @Override
    public List<UserSummaryResponse> getAllUsers() {
        logger.info("Fetching all users.");
        try {
            List<UserSummaryResponse> users = userRepository.findAll().stream()
                    .map(user -> new UserSummaryResponse(
                            user.getUserID(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getEmail(),
                            user.isActive()))
                    .collect(Collectors.toList());
            logger.debug("Successfully fetched {} users.", users.size());
            return users;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while fetching all users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve all users.", e);
        }
    }

    @Override
    public UserProfileResponse updateUserProfile(int userId, UpdateProfileRequest request) {
        logger.info("Attempting to update profile for user ID: {}", userId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            logger.debug("Found user {} for profile update.", user.getEmail());

            modelMapper.map(request, user);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            logger.info("User profile updated successfully for ID: {}", userId);
            return modelMapper.map(user, UserProfileResponse.class);
        } catch (UserNotFoundException e) {
            logger.warn("User profile update failed: User with ID {} not found. Error: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while updating profile for user ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update user profile.", e);
        }
    }

    @Override
    public UserProfileResponse updateProfileImage(int userId, UpdateProfileImageRequest request) {
        logger.info("Attempting to update profile image for user ID: {}", userId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            logger.debug("Found user {} for profile image update.", user.getEmail());

            // Directly set the profile image URL from the request
            user.setProfileimg(request.getProfileimg());
            user.setUpdatedAt(LocalDateTime.now()); // Update timestamp

            userRepository.save(user);
            logger.info("User profile image updated successfully for ID: {}", userId);

            // Map the updated user to a response DTO
            // IMPORTANT: Ensure your UserProfileResponse DTO has a 'profileimg' field
            // if you want this URL to be returned to the client.
            UserProfileResponse response = modelMapper.map(user, UserProfileResponse.class);
            // If ModelMapper doesn't automatically map profileimg from User to UserProfileResponse,
            // you might need to explicitly set it:
            // response.setProfileimg(user.getProfileimg());
            return response;
        } catch (UserNotFoundException e) {
            logger.warn("User profile image update failed: User with ID {} not found. Error: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while updating profile image for user ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update user profile image.", e);
        }
    }

    // Existing methods continue...
    @Override
    public void changePassword(ChangePasswordRequest request) {
        logger.info("Attempting to change password for user with email: {}", request.getEmail());
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            logger.debug("Found user {} to change password.", user.getEmail());

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            logger.info("Password changed successfully for user: {}", request.getEmail());
        } catch (UserNotFoundException e) {
            logger.warn("Password change failed: User with email {} not found. Error: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while changing password for user {}: {}", request.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to change password.", e);
        }
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        logger.info("Processing forgot password request for email: {}", request.getEmail());
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            logger.debug("Found user {} for forgot password.", user.getEmail());

            String verificationToken = "token-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 8);
            user.setVerificationToken(verificationToken);
            userRepository.save(user);
            logger.info("Verification token generated for user {}. Token (partially logged): {}", request.getEmail(), verificationToken.substring(0, 10) + "...");
        } catch (UserNotFoundException e) {
            logger.warn("Forgot password failed: User with email {} not found. Error: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred during forgot password for user {}: {}", request.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to process forgot password.", e);
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        logger.info("Attempting to reset password for user with email: {}", request.getEmail());
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            logger.debug("Found user {} for password reset.", user.getEmail());

            if (user.getVerificationToken() == null || !request.getToken().equals(user.getVerificationToken())) {
                logger.warn("Invalid or expired token provided for password reset for user: {}", request.getEmail());
                throw new RuntimeException("Invalid or expired token");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setVerificationToken(null);
            userRepository.save(user);
            logger.info("Password reset successfully for user: {}", request.getEmail());
        } catch (UserNotFoundException e) {
            logger.warn("Password reset failed: User with email {} not found. Error: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.warn("Password reset failed due to invalid token for user {}: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred during password reset for user {}: {}", request.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to reset password.", e);
        }
    }

    @Override
    public void recoverAccount(AccountRecoveryRequest request) {
        logger.info("Attempting account recovery for email: {}", request.getEmail());
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            logger.debug("Found user {} for account recovery.", user.getEmail());

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setActive(true);
            userRepository.save(user);
            logger.info("Account recovered and reactivated for user: {}", request.getEmail());
        } catch (UserNotFoundException e) {
            logger.warn("Account recovery failed: User with email {} not found. Error: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred during account recovery for user {}: {}", request.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to recover account.", e);
        }
    }

    @Override // This annotation is necessary as you have a method for handleFailedLogin in IUserService
    public void handleFailedLogin(String email) {
        logger.debug("Handling failed login attempt for email: {}", email);
        User user;
        try {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
        } catch (UserNotFoundException e) {
            logger.error("Failed login attempt for non-existent user email: {}. Error: {}", email, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while finding user {} for failed login handling: {}", email, e.getMessage(), e);
            throw new RuntimeException("Error during failed login handling.", e);
        }

        if (user.isBlocked() && user.getBlockedUntil().isAfter(LocalDateTime.now())) {
            logger.warn("Failed login attempt for already blocked user: {}. Blocked until: {}", user.getEmail(), user.getBlockedUntil());
            throw new RuntimeException("User is blocked. Try again later.");
        }

        try {
            user.setLoginAttempts(user.getLoginAttempts() + 1);
            logger.info("Incremented login attempts for user {}. Current attempts: {}", user.getEmail(), user.getLoginAttempts());

            if (user.getLoginAttempts() >= 3) {
                user.setBlocked(true);
                user.setBlockedUntil(LocalDateTime.now().plusMinutes(30));
                logger.warn("User {} has exceeded login attempts and is now blocked until: {}", user.getEmail(), user.getBlockedUntil());
            }

            userRepository.save(user);
            logger.debug("User {} login attempts updated and saved.", user.getEmail());
        } catch (Exception e) {
            logger.error("Error updating login attempts or blocking user {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to update user login attempts.", e);
        }
    }

    @Override
    public UserBlockStatusResponse getBlockStatus(String email) {
        logger.debug("Checking block status for user with email: {}", email);
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            logger.info("Block status for user {}: Blocked={}, Until={}", user.getEmail(), user.isBlocked(), user.getBlockedUntil());
            return new UserBlockStatusResponse(user.isBlocked(), user.getBlockedUntil());
        } catch (UserNotFoundException e) {
            logger.warn("Failed to get block status: User with email {} not found. Error: {}", email, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while getting block status for user {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user block status.", e);
        }
    }

    @Override
    public void unlockUser(String email) {
        logger.info("Attempting to unlock user with email: {}", email);
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            user.setBlocked(false);
            user.setBlockedUntil(null);
            user.setLoginAttempts(0);

            userRepository.save(user);
            logger.info("User {} unlocked successfully and login attempts reset.", user.getEmail());
        } catch (UserNotFoundException e) {
            logger.warn("Unlock user failed: User with email {} not found. Error: {}", email, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while unlocking user {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to unlock user.", e);
        }
    }

    @Override
    public void assignRoles(RoleAssignmentRequest request) {
        logger.info("Attempting to assign roles to user ID: {}. Roles: {}", request.getUserId(), request.getRoles());
        try {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            logger.debug("Found user {} for role assignment.", user.getEmail());

            user.setRoles(request.getRoles());
            userRepository.save(user);
            logger.info("Roles assigned successfully to user ID: {}. New roles: {}", request.getUserId(), request.getRoles());
        } catch (UserNotFoundException e) {
            logger.warn("Role assignment failed: User with ID {} not found. Error: {}", request.getUserId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while assigning roles to user ID {}: {}", request.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Failed to assign roles.", e);
        }
    }

    @Override
    public void updateUserStatus(int userId, UserStatusUpdateRequest request) {
        logger.info("Attempting to update status for user ID: {}. Active: {}, Blocked: {}", userId, request.isActive(), request.isBlocked());
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            logger.debug("Found user {} for status update.", user.getEmail());

            user.setActive(request.isActive());
            user.setBlocked(request.isBlocked());
            userRepository.save(user);
            logger.info("User status updated successfully for ID: {}. Active: {}, Blocked: {}", userId, user.isActive(), user.isBlocked());
        } catch (UserNotFoundException e) {
            logger.warn("User status update failed: User with ID {} not found. Error: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while updating status for user ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update user status.", e);
        }
    }

    @Override
    public void softdeleteUser(int userId) {
        logger.info("Attempting to delete (deactivate) user with ID: {}", userId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            logger.debug("Found user {} for deletion (deactivation).", user.getEmail());

            user.setActive(false);
            userRepository.save(user);
            logger.info("User ID {} successfully deactivated.", userId);
        } catch (UserNotFoundException e) {
            logger.warn("User deletion (deactivation) failed: User with ID {} not found. Error: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while deleting (deactivating) user ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete user.", e);
        }
    }

    @Override
    public void hardDeleteUser(int userId) {
        logger.warn("Attempting PERMANENTLY delete user with ID: {}. This action is IRREVERSIBLE and requires cross-service coordination.", userId);
        try {
            if (!userRepository.existsById(userId)) {
                logger.warn("Permanent user deletion failed: User with ID {} not found.", userId);
                throw new UserNotFoundException("User not found for permanent deletion.");
            }

            userRepository.deleteById(userId);
            logger.info("User ID {} PERMANENTLY DELETED from the database.", userId);

        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while permanently deleting user ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to permanently delete user due to an unexpected error.", e);
        }
    }
    @Override
    public int getUserId(int userId) {
        return userRepository.findById(userId)
                .map(User::getUserID)
                .orElseThrow(() -> new RuntimeException("User not found"));

    }

    @Override
    public List<CartItemDTO> getUserCartItems(Integer userId) {
        logger.info("Attempting to retrieve cart items for user ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            logger.warn("Cart item retrieval failed: User with ID {} not found in User Service.", userId);
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        try {
            ResponseEntity<List<CartItemDTO>> cartItemsResponse = cartServiceClient.getCartItems(userId);

            if (cartItemsResponse.getStatusCode().is2xxSuccessful() && cartItemsResponse.getBody() != null) {
                logger.info("Successfully retrieved {} cart items for user ID {} from Cart Service.",
                                cartItemsResponse.getBody().size(), userId);
                return cartItemsResponse.getBody();
            } else if (cartItemsResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.info("No cart found or cart is empty for user ID {} in Cart Service (Status 404). Returning empty list.", userId);
                return Collections.emptyList();
            } else {
                logger.error("Cart Service returned non-successful status for user ID {}: Status: {}, Body: {}",
                                userId, cartItemsResponse.getStatusCode(), cartItemsResponse.getBody());
                throw new RuntimeException("Failed to retrieve cart items from Cart Service. Status: " + cartItemsResponse.getStatusCode());
            }
        } catch (feign.FeignException.FeignClientException e) {
            logger.error("Client error from Cart Service when fetching cart items for user ID {}: Status: {}, Message: {}",
                           userId, e.status(), e.contentUTF8(), e);
            if (e.status() == HttpStatus.NOT_FOUND.value()) {
                logger.info("No cart found for user ID {} in Cart Service (FeignClientException 404). Returning empty list.", userId);
                return Collections.emptyList();
            }
            throw new RuntimeException("Error from Cart Service (client error) while fetching cart items for user " + userId + ": " + e.getMessage(), e);
        } catch (feign.FeignException.FeignServerException e) {
            logger.error("Server error from Cart Service when fetching cart items for user ID {}: Status: {}, Message: {}",
                           userId, e.status(), e.contentUTF8(), e);
            throw new RuntimeException("Cart Service internal error (server error) while fetching cart items for user " + userId + ": " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while communicating with Cart Service for user ID {}: {}",
                           userId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve cart items due to an unexpected error.", e);
        }
    }

    //get order details
    @Override
    public List<OrderDTO> getOrdersOfUser(int userId) {
        logger.info("Attempting to fetch orders for userId: {} from Order Microservice", userId);
        try {
            ResponseEntity<List<OrderDTO>> response = orderServiceClient.getOrdersByUserId(userId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Successfully fetched {} orders for userId: {}", response.getBody().size(), userId);
                return response.getBody();
            } else {
                logger.warn("Failed to fetch orders for userId: {}. Status: {}", userId, response.getStatusCode());
                // Handle specific HTTP statuses if needed (e.g., 404 for no orders found)
                return List.of(); // Return empty list if no orders or non-successful status
            }
        } catch (feign.FeignException.NotFound e) {
            logger.warn("No orders found or user not found in Order Microservice for userId: {}. Error: {}", userId, e.getMessage());
            return List.of(); // Return empty list for 404 (no orders or user not found)
        } catch (feign.FeignException e) {
            logger.error("Error fetching orders for userId: {} from Order Microservice. Status: {}, Message: {}", userId, e.status(), e.getMessage());
            // Re-throw or handle more gracefully based on your error handling strategy
            throw new RuntimeException("Failed to fetch orders from Order Service for user " + userId, e);
        }
    }
    
    
    // --- NEW: Implementation for Authentication Service to fetch user details by email ---
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserAuthDetailsDto getUserAuthDetailsByEmail(String email) {
        logger.debug("Attempting to fetch user authentication details for email: {}", email);
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            logger.warn("User not found for authentication details lookup: {}", email);
            return null; // Or throw UserNotFoundException if you want to propagate specific exceptions
        }

        User user = userOptional.get();
        UserAuthDetailsDto dto = new UserAuthDetailsDto();
        dto.setUserId(user.getUserID());
        dto.setUsername(user.getEmail()); // Assuming 'email' is used as 'username' for login
        dto.setPassword(user.getPassword()); // This must be the ENCODED password

        // Map roles from Set<Role> to List<String>
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                               .map(Role::name) // Assuming Role enum has `name()` to get string value
                               .collect(Collectors.toList()));
        } else {
            dto.setRoles(Collections.emptyList());
        }

        logger.info("Successfully fetched authentication details for user: {}", email);
        return dto;
    }
}