package com.abcm0018.inventarioautomatizado.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterUserRequest {
    @NotBlank(message = "Username cannot be null or empty")
    @Pattern(
            regexp = "^[a-zA-Z][a-zA-Z0-9_]{2,19}$",
            message = "Username must start with a letter, contain only letters, numbers or underscores, and be 3-20 characters long"
    )
    private String username;
    @NotBlank(message = "Name cannot be null or empty")
    private String name;
    @NotBlank(message = "Surname cannot be null or empty")
    private String surname;
    @NotBlank(message = "Password cannot be null or empty")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "The password must contain at least one uppercase letter, one lowercase letter, one number, and one special character."
    )
    @Size(min = 8, message = "The password must be at least 8 characters long.")
    private String password;
}
