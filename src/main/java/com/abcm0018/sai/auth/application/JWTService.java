package com.abcm0018.sai.auth.application;


import com.abcm0018.sai.users.domain.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface JWTService {
    String getEmployeeNumberFromToken(String token);

    String generateToken(User user);

    String generateRefreshToken(User user);

    boolean isTokenValid(String token, UserDetails userDetails);
}
