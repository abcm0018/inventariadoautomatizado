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
}