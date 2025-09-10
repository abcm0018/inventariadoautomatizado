package com.abcm0018.inventarioautomatizado.users.domain.repository;

import com.abcm0018.inventarioautomatizado.users.domain.entity.Role;
import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);
}
