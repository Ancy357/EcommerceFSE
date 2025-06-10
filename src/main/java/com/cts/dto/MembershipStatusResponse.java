package com.cts.dto;

import com.cts.enums.MembershipLevel;
import lombok.Data;

@Data
public class MembershipStatusResponse {
    private MembershipLevel membershipLevel;
    private String status;
}


