package com.abcm0018.sai.auth.application.service.impl;


import com.abcm0018.sai.auth.application.service.AuthService;
import com.abcm0018.sai.auth.application.JWTService;
import com.abcm0018.sai.auth.application.TokenBlackList;
import com.abcm0018.sai.auth.application.dtos.AuthResponseDTO;
import com.abcm0018.sai.auth.application.dtos.LoginRequest;
import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.users.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final TokenBlackList tokenBlackList;


    @Override
    public AuthResponseDTO loginUser(LoginRequest request) {
        Authentication authentication;
        try{
            authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmployeeNumber(), request.getPassword()));
        }  catch (AuthenticationException e) {
            throw new AuthenticationServiceException("Error al autenticar el usuario");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User userDetails = userRepository.findByEmployeeNumber(request.getEmployeeNumber()).orElseThrow();

        String token = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponseDTO.builder()
				.email(userDetails.getEmail())
				.role(userDetails.getRole().getDisplayName())
				.employeeNumber(userDetails.getEmployeeNumber())
				.token(token)
				.refreshToken(refreshToken)
				.build();

    }


    @Override
    public AuthResponseDTO renewUserToken(String userToken) {
        AuthResponseDTO response;
        String employeeNumber = jwtService.getEmployeeNumberFromToken(userToken);

        // Buscamos el usuario por su nombre
        User user = userRepository.findByEmployeeNumber(employeeNumber).orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Generamos el nuevo token
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        response =  AuthResponseDTO.builder()
				.email(user.getEmail())
				.role(user.getRole().getDisplayName())
				.employeeNumber(user.getEmployeeNumber())
				.token(token)
				.refreshToken(refreshToken)
				.build();

        return response;
    }

    @Override
    public String logout(String token) {
        tokenBlackList.addToBlackList(token);
        SecurityContextHolder.clearContext();
        return "Logget out succesfully";
    }

    private boolean employeeNumberExists(String employeeNumber) {
        return userRepository.findByEmployeeNumber(employeeNumber).isPresent();
    }
}
