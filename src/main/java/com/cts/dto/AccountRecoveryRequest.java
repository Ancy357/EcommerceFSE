package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountRecoveryRequest {
    private String email;
    private String newPassword;
}
