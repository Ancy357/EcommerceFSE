package com.cts.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryResponse {
    private int userID;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isActive;
}
