package com.example.demo.customers.validator;

import com.example.demo.exceptions.BadRequestException;
import com.example.demo.customers.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerValidator {

    public void validate(Customer customer) {

        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new BadRequestException("Name is required");
        }

        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
    }
}