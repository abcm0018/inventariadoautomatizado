package com.abcm0018.inventarioautomatizado.auth.service.impl;


import com.abcm0018.inventarioautomatizado.auth.dtos.AuthResponseDTO;
import com.abcm0018.inventarioautomatizado.auth.dtos.LoginRequest;
import com.abcm0018.inventarioautomatizado.auth.dtos.PasswordResetRequest;
import com.abcm0018.inventarioautomatizado.auth.dtos.RegisterUserRequest;
import com.abcm0018.inventarioautomatizado.auth.exceptions.AuthServiceException;
import com.abcm0018.inventarioautomatizado.auth.service.AuthService;
import com.abcm0018.inventarioautomatizado.auth.service.EmailService;
import com.abcm0018.inventarioautomatizado.auth.service.JWTService;
import com.abcm0018.inventarioautomatizado.auth.service.TokenBlackList;
import com.abcm0018.inventarioautomatizado.productos.constants.CustomErrorCode;
import com.abcm0018.inventarioautomatizado.shared.config.InventariadoCacheConfig;
import com.abcm0018.inventarioautomatizado.users.domain.entity.Role;
import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import com.abcm0018.inventarioautomatizado.users.domain.repository.UserRepository;
import com.abcm0018.inventarioautomatizado.users.dtos.UserResponseDTO;
import com.abcm0018.inventarioautomatizado.users.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@CacheConfig(cacheNames = InventariadoCacheConfig.PRODUCT_INFO)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final TokenBlackList tokenBlackList;
    private final EmailService emailService;

    @Override
    @CacheEvict(allEntries = true)
    public Integer addUser(RegisterUserRequest request) {
        if(employeeNumberExists(request.getEmployeeNumber())){
            throw new AuthServiceException(CustomErrorCode.BAD_REQUEST,
                    "There is already a operator user with the employee number: " + request.getEmployeeNumber(),
                    HttpStatus.BAD_REQUEST);
        }

        String password = KeyGenerators.string().generateKey();

        User user = User.builder()
                .employeeNumber(request.getEmployeeNumber())
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(password))
                .jobPosition(request.getJobPosition())
                .role(Role.valueOf(request.getRole()))
                .build();
        try{
            userRepository.save(user);
            // Enviar correo electronico al usuario con la contraseña (librería de email)
            String subject = "Tu cuenta ha sido creada";
            String body = String.format(
                    "Hola %s %s,\n\nTu número de empleado es: %s\nTu contraseña temporal es: %s",
                    user.getName(), user.getSurname(), user.getEmployeeNumber(), password
            );
            emailService.sendEmail(user.getEmail(), subject, body);

            log.info("User password added: {}", password);
            return 1;
        } catch (Exception e){
            throw new AuthServiceException(
                    CustomErrorCode.INTERNAL_SERVER_ERROR,
                    "Error saving a user: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    @CacheEvict(allEntries = true)
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
        UserResponseDTO userResponse =   UserMapper.toDTO(userDetails);

        return AuthResponseDTO.builder().
                user(userResponse).
                token(token).
                refreshToken(refreshToken).
                build();

    }

    @Override
    @CacheEvict(allEntries = true)
    public UserResponseDTO updatePassword(PasswordResetRequest passReset) {
        User user = userRepository.findByEmployeeNumber(passReset.getEmployeeNumber()).orElseThrow();

        // Si las contraseñas son iguales, lanzar un error
        if(passwordEncoder.matches(passReset.getPassword(), user.getPassword())) {
            throw new AuthServiceException(
                    CustomErrorCode.BAD_REQUEST,
                    "La nueva contraseña no puede ser igual a la anterior.",
                    HttpStatus.BAD_REQUEST
            );
        }

        user.setPassword(passwordEncoder.encode(passReset.getPassword()));

        return UserMapper.toDTO(userRepository.saveAndFlush(user));
    }

    @Override
    @CacheEvict(allEntries = true)
    public AuthResponseDTO renewUserToken(String userToken) {
        AuthResponseDTO response;
        String employeeNumber = jwtService.getEmployeeNumberFromToken(userToken);

        // Buscamos el usuario por su nombre
        User user = userRepository.findByEmployeeNumber(employeeNumber).orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserResponseDTO userResponse =   UserMapper.toDTO(user);
        // Generamos el nuevo token
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        response =  AuthResponseDTO.builder().user(userResponse).token(token).refreshToken(refreshToken).build();

        return response;
    }

    @Override
    @CacheEvict(allEntries = true)
    public String logout(String token) {
        tokenBlackList.addToBlackList(token);
        SecurityContextHolder.clearContext();
        return "Logget out succesfully";
    }

    private boolean employeeNumberExists(String employeeNumber) {
        return userRepository.findByEmployeeNumber(employeeNumber).isPresent();
    }


}
