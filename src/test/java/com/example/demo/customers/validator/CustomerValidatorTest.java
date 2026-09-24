package com.example.demo.customers.validator;

import com.example.demo.customers.model.Customer;
import com.example.demo.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerValidatorTest {

    private final CustomerValidator validator = new CustomerValidator();

    @Test
    void rejectsCustomerWithoutName() {
        Customer customer = validCustomer();
        customer.setName(" ");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> validator.validate(customer));

        assertEquals("Name is required", exception.getMessage());
    }

    @Test
    void rejectsCustomerWithoutEmail() {
        Customer customer = validCustomer();
        customer.setEmail(null);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> validator.validate(customer));

        assertEquals("Email is required", exception.getMessage());
    }

    @Test
    void acceptsCustomerWithNameAndEmail() {
        assertDoesNotThrow(() -> validator.validate(validCustomer()));
    }

    private Customer validCustomer() {
        Customer customer = new Customer();
        customer.setName("Maria Silva");
        customer.setEmail("maria@example.com");
        return customer;
    }
}
