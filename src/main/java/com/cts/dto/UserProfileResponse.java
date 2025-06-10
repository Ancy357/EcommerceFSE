package com.cts.dto;

import com.cts.enums.Role;
import com.cts.enums.MembershipLevel;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserProfileResponse {
    private int userID;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private LocalDate dateOfBirth;
    private String gender;
    private Set<Role> roles;
    private MembershipLevel membershipLevel;
    private boolean isActive;
    private boolean isBlocked;
}
