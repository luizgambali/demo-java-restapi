package com.example.demo.customers.dto;

public record UpdateCustomerRequest(
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