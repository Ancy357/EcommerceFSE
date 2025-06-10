package com.cts.dto;

import java.util.Set;

import com.cts.enums.Role;

import lombok.Data;

@Data
public class RoleAssignmentRequest {
    //private String email;
    private int userId;
    private Set<Role> roles;
}