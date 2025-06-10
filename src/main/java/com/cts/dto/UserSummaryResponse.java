package com.cts.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSummaryResponse {
    private int userID;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isActive;
}
