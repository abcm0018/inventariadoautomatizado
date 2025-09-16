package com.abcm0018.inventarioautomatizado.auth.dtos;

import jakarta.validation.constraints.Email;
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
    @NotBlank(message = "Employee number cannot be null or empty")
    @Pattern(
            regexp = "^[a-zA-Z][a-zA-Z0-9_]{2,19}$",
            message = "Employee number must start with a letter, contain only letters, numbers or underscores, and be 3-20 characters long"
    )
    private String employeeNumber;
    @NotBlank(message = "Name cannot be null or empty")
    private String name;
    @NotBlank(message = "Surname cannot be null or empty")
    private String surname;
    @Email(message = "Email cannot be null or empty")
    private String email;
    @NotBlank(message = "Job position cannot be null or empty")
    private String jobPosition;
    @NotBlank(message = "Role cannot be null or empty")
    private String role;
}
