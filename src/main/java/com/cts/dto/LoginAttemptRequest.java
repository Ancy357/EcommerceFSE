package com.cts.dto;

import lombok.Data;

@Data
public class LoginAttemptRequest {
    private String email;
    private String ipAddress;
    private boolean successful;
}
