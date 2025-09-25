package com.abcm0018.inventarioautomatizado.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {
    @NotBlank(message = "Employee number cannot be null or empty")
    @Pattern(
            regexp = "^[0-9]{8}[A-Z]$",
            message = "Employee number must contain only numbers"
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
    private String role;
    private Boolean active;
    private Boolean blocked;
    private Boolean expirated;
}