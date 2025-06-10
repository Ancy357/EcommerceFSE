
package com.cts.service;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ModelMapper modelMapper;

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Check if user is blocked
        if (user.isBlocked() && user.getBlockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("User is blocked until " + user.getBlockedUntil());
        }

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user.getEmail());
            throw new RuntimeException("Invalid credentials");
        }

        // Successful login: reset login attempts
        user.setLoginAttempts(0);
        userRepository.save(user);

        return "Login successful"; // Replace with JWT token when security is implemented
    }

    
    @Override
    public RegisterResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }
        User user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setRoles(Set.of(Role.USER));
        userRepository.save(user);
        return new RegisterResponse("User registered successfully", user.getEmail());
    }

    @Override
    public UserProfileResponse getUserById(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return modelMapper.map(user, UserProfileResponse.class);
    }

    @Override
    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserSummaryResponse(
                        user.getUserID(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.isActive()))
                .collect(Collectors.toList());
    }

    @Override
    public UserProfileResponse updateUserProfile(int userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        modelMapper.map(request, user);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return modelMapper.map(user, UserProfileResponse.class);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setLastPasswordChangeAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setVerificationToken("token-" + System.currentTimeMillis());
        userRepository.save(user);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!request.getToken().equals(user.getVerificationToken())) {
            throw new RuntimeException("Invalid token");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    @Override
    public void recoverAccount(AccountRecoveryRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
  
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setActive(true);
        userRepository.save(user);
    }

    public void handleFailedLogin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.isBlocked() && user.getBlockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("User is blocked. Try again later.");
        }

        // Increment login attempts
        user.setLoginAttempts(user.getLoginAttempts() + 1);

        if (user.getLoginAttempts() >= 3) {
            user.setBlocked(true);
            user.setBlockedUntil(LocalDateTime.now().plusMinutes(30)); // Block for 30 minutes
        }

        userRepository.save(user);
    }

    
    @Override
    public UserBlockStatusResponse getBlockStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return new UserBlockStatusResponse(user.isBlocked(), user.getBlockedUntil());
    }
    
    public void unlockUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setBlocked(false);
        user.setBlockedUntil(null);
        user.setLoginAttempts(0);

        userRepository.save(user);
    }

    @Override
    public void assignRoles(RoleAssignmentRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setRoles(request.getRoles());
        userRepository.save(user);
    }

    @Override
    public void updateUserStatus(int userId, UserStatusUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setActive(request.isActive());
        user.setBlocked(request.isBlocked());
        userRepository.save(user);
    }

    @Override
    public void deleteUser(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }
    
//    @Override
//    public void updateMembershipLevel(int userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new UserNotFoundException("User not found"));
//
//        int orderCount = user.getTotalOrders();
//
//        MembershipLevel updatedLevel = 
//            orderCount >= 20 ? MembershipLevel.PLATINUM :
//            orderCount >= 10 ? MembershipLevel.GOLD :
//            orderCount >= 5 ? MembershipLevel.SILVER :
//            MembershipLevel.BASIC;
//
//        user.setMembershipLevel(updatedLevel);
//        
//        // Use ModelMapper to convert user to a DTO if needed
//        UserProfileResponse response = modelMapper.map(user, UserProfileResponse.class);
//        
//        userRepository.save(user);
//    }

    
    
}
