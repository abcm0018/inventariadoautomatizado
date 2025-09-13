package com.abcm0018.inventarioautomatizado.users.service;

import com.abcm0018.inventarioautomatizado.users.dtos.UserRequest;
import com.abcm0018.inventarioautomatizado.users.dtos.UserResponseDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO updateUser(String employeeNumber, UserRequest userRequest);
    void deleteUser(String employeeNumber);
    UserResponseDTO getUser(String employeeNumber);
    List<UserResponseDTO> getAllUsers();
}
