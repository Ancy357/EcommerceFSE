package com.cts.dto;


import com.cts.enums.Role;
import lombok.Data;

import java.util.Set;

@Data
public class RoleUpdateRequest {
    private Set<Role> roles;
}