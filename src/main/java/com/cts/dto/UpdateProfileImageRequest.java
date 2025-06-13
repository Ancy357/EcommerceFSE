package com.cts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileImageRequest {

    @NotBlank(message = "Profile image URL cannot be empty")
    @Size(max = 2048, message = "Profile image URL cannot exceed 2048 characters") // Standard max length for URLs
    // Basic regex for URL validation (allows http, https, and a general structure)
    @Pattern(regexp = "^(http|https)://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/[^ \"' لذا]*)?$",
             message = "Profile image URL must be a valid HTTP or HTTPS URL")
    private String profileImageUrl;
}