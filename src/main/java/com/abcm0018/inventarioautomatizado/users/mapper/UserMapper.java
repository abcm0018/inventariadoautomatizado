package com.abcm0018.inventarioautomatizado.users.mapper;

import com.abcm0018.inventarioautomatizado.productos.domain.entity.Product;
import com.abcm0018.inventarioautomatizado.productos.mappers.ProductMapper;
import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import com.abcm0018.inventarioautomatizado.users.dtos.UserRequest;
import com.abcm0018.inventarioautomatizado.users.dtos.UserResponseDTO;

import java.util.List;

public class UserMapper {
    private UserMapper(){
        throw new IllegalStateException("Utility class");
    }
    public static UserResponseDTO toDTO(User user){
        return UserResponseDTO
                .builder()
                .employeeNumber(user.getEmployeeNumber())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .jobPosition(user.getJobPosition())
                .role(user.getRole().name().toUpperCase())
                .build();
    }

    public static User toEntity(UserRequest dto){
        return User
                .builder()
                .employeeNumber(dto.getEmployeeNumber())
                .name(dto.getName())
                .surname(dto.getSurname())
                .email(dto.getEmail())
                .jobPosition(dto.getJobPosition())
                .build();
    }

    public static List<UserResponseDTO> toDTOList(List<User> userList) {
        return userList.stream().map(UserMapper::toDTO).toList();
    }
}