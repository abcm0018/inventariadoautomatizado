package com.abcm0018.inventarioautomatizado.users.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private UserResponse user;
    //private String token;
    //private String refreshToken;
}
