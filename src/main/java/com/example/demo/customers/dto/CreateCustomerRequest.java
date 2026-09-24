package com.example.demo.customers.dto;

public record CreateCustomerRequest(
        String name,
        String address,
        String city,
        String country,
        String zipcode,
        String phone,
        String email,
        boolean active
) {
}