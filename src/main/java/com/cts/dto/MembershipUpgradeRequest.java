package com.cts.dto;

import com.cts.enums.MembershipLevel;
import lombok.Data;

@Data
public class MembershipUpgradeRequest {
    private MembershipLevel membershipLevel;
}
