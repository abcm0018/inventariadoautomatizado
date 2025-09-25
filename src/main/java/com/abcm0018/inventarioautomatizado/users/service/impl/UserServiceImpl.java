package com.abcm0018.inventarioautomatizado.users.service.impl;

import com.abcm0018.inventarioautomatizado.productos.constants.CustomErrorCode;
import com.abcm0018.inventarioautomatizado.productos.domain.entity.Product;
import com.abcm0018.inventarioautomatizado.productos.mappers.ProductMapper;
import com.abcm0018.inventarioautomatizado.shared.config.InventariadoCacheConfig;
import com.abcm0018.inventarioautomatizado.shared.utils.UserUtils;
import com.abcm0018.inventarioautomatizado.timesheet.domain.entity.Timesheet;
import com.abcm0018.inventarioautomatizado.timesheet.domain.repository.TimesheetRepository;
import com.abcm0018.inventarioautomatizado.timesheet.dtos.TimesheetResponseDTO;
import com.abcm0018.inventarioautomatizado.timesheet.mapper.TimesheetMapper;
import com.abcm0018.inventarioautomatizado.users.domain.entity.Role;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@CacheConfig(cacheNames = InventariadoCacheConfig.PRODUCT_INFO)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TimesheetRepository timesheetRepository;

    @Override
    @CacheEvict(allEntries = true)
    public UserResponseDTO updateUser(String employeeNumber, UserRequest userRequest) {
        if (!UserUtils.isEmployeeNumberValid(employeeNumber)) {
            throw new UserServiceException(CustomErrorCode.BAD_REQUEST, "The employee number to be updated cannot be empty.", HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findByEmployeeNumber(employeeNumber).orElseThrow(() ->
                new UserServiceException(CustomErrorCode.NOT_FOUND,
                        "The user with employee number " + employeeNumber + " doesn't exist",
                        HttpStatus.NOT_FOUND));

        User userToUpdate = UserMapper.toEntity(userRequest);
        userToUpdate.setId(user.getId());
        userToUpdate.setPassword(user.getPassword());
        userToUpdate.setRole(user.getRole());
        userToUpdate.setRegistrationDate(user.getRegistrationDate());

        if(userRequest.getActive() != null){
            userToUpdate.setActive(userRequest.getActive());
        }

        if(userRequest.getExpirated() != null){
            userToUpdate.setExpirated(userRequest.getExpirated());
        }

        if(userRequest.getBlocked() != null){
            userToUpdate.setBlocked(userRequest.getBlocked());
        }

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
                        HttpStatus.NOT_FOUND));

        if(user.isActive()){
            throw new UserServiceException(CustomErrorCode.CONFLICT,
                    "The user with employee number " + employeeNumber + " cannot be deleted",
                    HttpStatus.CONFLICT);
        }

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
                        HttpStatus.NOT_FOUND));

        return UserMapper.toDTO(user);
    }

    @Override
    public List<UserResponseDTO> getUsersByRole(String role) {
        List<User> userList = userRepository.findByRole(Role.valueOf(role.toUpperCase()));
        return UserMapper.toDTOList(userList);
    }

    @Override
    @CacheEvict(allEntries = true)
    public List<UserResponseDTO> getAllUsers() {
        List<User> userList = userRepository.findAll();
        return UserMapper.toDTOList(userList);
    }
}
