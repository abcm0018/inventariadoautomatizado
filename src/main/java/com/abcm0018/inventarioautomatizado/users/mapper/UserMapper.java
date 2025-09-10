package com.abcm0018.inventarioautomatizado.users.mapper;

import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import com.abcm0018.inventarioautomatizado.users.dtos.UserRequest;
import com.abcm0018.inventarioautomatizado.users.dtos.UserResponse;

import java.util.List;

public class UserMapper {
    private UserMapper(){
        throw new IllegalStateException("Utility class");
    }
    public static UserResponse toDTO(User user){
        return UserResponse
                .builder()
                .username(user.getUsername())
                .name(user.getName())
                .surname(user.getSurname())
                .role(user.getRole().name().toLowerCase())
                .build();
    }

    public static User toEntity(UserRequest dto){
        return User
                .builder()
                .username(dto.getUsername())
                .name(dto.getName())
                .surname(dto.getSurname())
                .build();
    }
}