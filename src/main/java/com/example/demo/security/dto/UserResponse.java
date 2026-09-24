package com.example.demo.security.dto;

import com.example.demo.security.model.Role;

import java.util.UUID;

public record UserResponse(UUID id, String username, boolean active, Role role) { }