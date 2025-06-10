package com.cts.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserBlockStatusResponse {
    private boolean isBlocked;
    private LocalDateTime blockedUntil;
}
