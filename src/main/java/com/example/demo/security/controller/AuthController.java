package com.example.demo.security.controller;

import com.example.demo.security.dto.LoginRequest;
import com.example.demo.security.dto.LoginResponse;
import com.example.demo.security.dto.RegisterRequest;
import com.example.demo.security.dto.UserResponse;
import com.example.demo.security.model.User;
import com.example.demo.security.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {

        UserResponse user = userService.createUser(request.username(), request.password());

        return ResponseEntity.status(201).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request)
    {
        LoginResponse response = userService.login(request);

        return ResponseEntity.ok(response);
    }
}