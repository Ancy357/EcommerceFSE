
package com.cts.service;

import com.cts.dto.*;
import com.cts.entity.User;
import com.cts.enums.Role;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface IUserService {

    // Registration and Login
    String login(LoginRequest request);
    RegisterResponse registerUser(RegisterRequest request);

    // Profile Management
    UserProfileResponse getUserById(int userId);
    List<UserSummaryResponse> getAllUsers();
    UserProfileResponse updateUserProfile(int userId, UpdateProfileRequest request);
    void changePassword(ChangePasswordRequest request);

    // Password Recovery
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    void recoverAccount(AccountRecoveryRequest request);

    // Login Attempt Tracking
    void handleFailedLogin(String email);
    UserBlockStatusResponse getBlockStatus(String email);
    void unlockUser(String email);

    // Role and Status Management
    void assignRoles(RoleAssignmentRequest request);
    void updateUserStatus(int userId, UserStatusUpdateRequest request);

    // Delete User
    void softdeleteUser(int userId);
    void hardDeleteUser(int userId);
    
    //Update membership
    //public void updateMembershipLevel(int userId);
    
    //methods for feign client
    int getUserId(int userId);
    List<CartItemDTO> getUserCartItems(Integer userId);
    
    //For authentication
    public Optional<User> findByEmail(String email);
    UserAuthDetailsDto getUserAuthDetailsByEmail(String email);

}
