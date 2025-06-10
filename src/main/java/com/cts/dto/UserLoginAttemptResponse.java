package com.cts.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserLoginAttemptResponse {
    private int loginAttempts;
    private LocalDateTime blockedUntil;
}
