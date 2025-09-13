package com.abcm0018.inventarioautomatizado.auth.dtos;

import com.abcm0018.inventarioautomatizado.users.dtos.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private UserResponseDTO user;
    //private String token;
    //private String refreshToken;
}
