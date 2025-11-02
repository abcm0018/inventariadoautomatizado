package com.abcm0018.sai.auth.application.service;

import com.abcm0018.sai.auth.application.dtos.AuthResponseDTO;
import com.abcm0018.sai.auth.application.dtos.LoginRequest;

public interface AuthService {
    AuthResponseDTO loginUser(LoginRequest request);
    AuthResponseDTO renewUserToken(String userToken);
    String logout(String token);
}
