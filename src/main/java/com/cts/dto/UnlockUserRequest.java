package com.cts.dto;

import lombok.Data;

@Data
public class UnlockUserRequest {
    private String email;
    private String adminNote;
}