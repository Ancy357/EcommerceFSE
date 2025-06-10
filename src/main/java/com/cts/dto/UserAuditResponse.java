package com.cts.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAuditResponse {
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}