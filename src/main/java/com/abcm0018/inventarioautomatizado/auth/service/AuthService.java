package com.abcm0018.inventarioautomatizado.auth.service;

import com.abcm0018.inventarioautomatizado.auth.dtos.AuthResponseDTO;
import com.abcm0018.inventarioautomatizado.auth.dtos.LoginRequest;
import com.abcm0018.inventarioautomatizado.auth.dtos.PasswordResetRequest;
import com.abcm0018.inventarioautomatizado.auth.dtos.RegisterUserRequest;
import com.abcm0018.inventarioautomatizado.users.dtos.UserResponseDTO;

public interface AuthService {
    Integer addUser(RegisterUserRequest request);
    AuthResponseDTO loginUser(LoginRequest request);
    UserResponseDTO updatePassword(PasswordResetRequest passReset);
    AuthResponseDTO renewUserToken(String userToken);
    String logout(String token);

}
