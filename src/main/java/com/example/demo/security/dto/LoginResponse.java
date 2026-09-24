package com.example.demo.security.dto;

import java.util.UUID;

public record LoginResponse(UUID id, String username, String token) { }
