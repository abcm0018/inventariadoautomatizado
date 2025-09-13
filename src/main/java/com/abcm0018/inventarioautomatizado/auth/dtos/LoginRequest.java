package com.abcm0018.inventarioautomatizado.auth.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest {
    @NotBlank(message = "Username cannot be null or empty")
    @Pattern(
            regexp = "^[a-zA-Z][a-zA-Z0-9_]{2,19}$",
            message = "Username must start with a letter, contain only letters, numbers or underscores, and be 3-20 characters long"
    )
    private String useranme;
    @NotNull(message = "Password cannot be null or empty")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "The password must contain at least one uppercase letter, one lowercase letter, one number, and one special character."
    )
    @Size(min = 8, message = "The password must be at least 8 characters long.")
    private String password;
}

