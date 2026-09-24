package com.example.demo.security.config;

import com.example.demo.security.model.Role;
import com.example.demo.security.model.User;
import com.example.demo.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdminSeed implements CommandLineRunner {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminSeed(
            UserRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.username:}") String username,
            @Value("${app.admin.password:}") String password) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) {

        if (username.isBlank() || password.isBlank()) {
            return;
        }

        boolean hasAdmin = repository.existsByRole(Role.ADMIN);

        if (!hasAdmin) {
            User admin = new User();

            admin.setId(UUID.randomUUID());
            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setActive(true);
            admin.setRole(Role.ADMIN);

            repository.save(admin);
        }
    }
}
