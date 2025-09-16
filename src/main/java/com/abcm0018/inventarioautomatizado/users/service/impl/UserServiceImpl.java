package com.abcm0018.inventarioautomatizado.users.service.impl;

import com.abcm0018.inventarioautomatizado.productos.constants.CustomErrorCode;
import com.abcm0018.inventarioautomatizado.productos.domain.entity.Product;
import com.abcm0018.inventarioautomatizado.productos.mappers.ProductMapper;
import com.abcm0018.inventarioautomatizado.shared.config.InventariadoCacheConfig;
import com.abcm0018.inventarioautomatizado.shared.utils.UserUtils;
import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import com.abcm0018.inventarioautomatizado.users.domain.repository.UserRepository;
import com.abcm0018.inventarioautomatizado.users.dtos.UserRequest;
import com.abcm0018.inventarioautomatizado.users.dtos.UserResponseDTO;
import com.abcm0018.inventarioautomatizado.users.exceptions.UserServiceException;
import com.abcm0018.inventarioautomatizado.users.mapper.UserMapper;
import com.abcm0018.inventarioautomatizado.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@CacheConfig(cacheNames = InventariadoCacheConfig.PRODUCT_INFO)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @CacheEvict(allEntries = true)
    public UserResponseDTO updateUser(String employeeNumber, UserRequest userRequest) {
        if (!UserUtils.isEmployeeNumberValid(employeeNumber)) {
            throw new UserServiceException(CustomErrorCode.BAD_REQUEST, "The employee number to be updated cannot be empty.", HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findByEmployeeNumber(employeeNumber).orElseThrow(() ->
                new UserServiceException(CustomErrorCode.NOT_FOUND,
                        "The user with employee number " + employeeNumber + " doesn't exist",
                        HttpStatus.BAD_REQUEST));

        User userToUpdate = UserMapper.toEntity(userRequest);
        userToUpdate.setId(user.getId());
        userToUpdate.setPassword(user.getPassword());
        userToUpdate.setRole(user.getRole());

        userRepository.save(userToUpdate);

        return UserMapper.toDTO(userToUpdate);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void deleteUser(String employeeNumber) {
        if (!UserUtils.isEmployeeNumberValid(employeeNumber)) {
            throw new UserServiceException(CustomErrorCode.BAD_REQUEST, "The employee number to be deleted cannot be empty.", HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findByEmployeeNumber(employeeNumber).orElseThrow(() ->
                new UserServiceException(CustomErrorCode.NOT_FOUND,
                        "The user with employee number " + employeeNumber + " doesn't exist",
                        HttpStatus.BAD_REQUEST));

        userRepository.delete(user);
    }

    @Override
    @CacheEvict(allEntries = true)
    public UserResponseDTO getUser(String employeeNumber) {
        if (!UserUtils.isEmployeeNumberValid(employeeNumber)) {
            throw new UserServiceException(CustomErrorCode.BAD_REQUEST, "The employee number to be deleted cannot be empty.", HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findByEmployeeNumber(employeeNumber).orElseThrow(() ->
                new UserServiceException(CustomErrorCode.NOT_FOUND,
                        "The user with employee number " + employeeNumber + " doesn't exist",
                        HttpStatus.BAD_REQUEST));
        return UserMapper.toDTO(user);
    }

    @Override
    public List<UserResponseDTO> getUsersByRole(String role) {
        List<User> userList = userRepository.findByRole(role);
        return UserMapper.toDTOList(userList);
    }

    @Override
    @CacheEvict(allEntries = true)
    public List<UserResponseDTO> getAllUsers() {
        List<User> userList = userRepository.findAll();
        return UserMapper.toDTOList(userList);
    }
}
