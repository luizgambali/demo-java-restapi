package com.example.demo.security.service;

import com.example.demo.exceptions.NotFoundException;
import com.example.demo.security.model.User;
import com.example.demo.security.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository repository;

    public CurrentUserService(UserRepository repository) {
        this.repository = repository;
    }

    public User get() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        return repository.findByUsername(username).orElseThrow(() -> new NotFoundException("Logged user not found"));
    }

}
