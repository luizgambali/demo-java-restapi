package com.example.demo.security.repository;

import com.example.demo.security.model.Role;
import com.example.demo.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);
    boolean existsByRole(Role role);
    int countByRole(Role role);
}
