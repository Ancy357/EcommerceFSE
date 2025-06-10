package com.cts.dto;
import lombok.Data;

@Data
public class UserStatusUpdateRequest {
    private boolean isActive;
    private boolean isBlocked;
}