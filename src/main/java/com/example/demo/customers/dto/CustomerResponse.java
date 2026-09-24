package com.example.demo.customers.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String address,
        String city,
        String country,
        String zipcode,
        String phone,
        String email,
        LocalDateTime createdAt,
        boolean active
) {
}