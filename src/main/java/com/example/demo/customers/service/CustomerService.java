package com.example.demo.customers.service;

import com.example.demo.customers.dto.CreateCustomerRequest;
import com.example.demo.customers.dto.CustomerResponse;
import com.example.demo.customers.dto.DeleteCustomerRequest;
import com.example.demo.customers.dto.UpdateCustomerRequest;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.customers.model.Customer;
import com.example.demo.customers.repository.CustomerRepository;
import com.example.demo.customers.validator.CustomerValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerValidator validator;
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository, CustomerValidator validator)
    {
        this.repository = repository;
        this.validator = validator;
    }

    public List<CustomerResponse> findAll()
    {
        return repository.findAll()
                .stream()
                .map(customer -> new CustomerResponse(
                        customer.getId(),
                        customer.getName(),
                        customer.getAddress(),
                        customer.getCity(),
                        customer.getCountry(),
                        customer.getZipcode(),
                        customer.getPhone(),
                        customer.getEmail(),
                        customer.getCreatedAt(),
                        customer.isActive()
                ))
                .toList();
    }

    public CustomerResponse findById(UUID id)
    {
        Customer customer = repository.findById(id).orElseThrow(() -> new NotFoundException("Customer not found"));

        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getAddress(),
                customer.getCity(),
                customer.getCountry(),
                customer.getZipcode(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getCreatedAt(),
                customer.isActive()
        );
    }

    public CustomerResponse add(CreateCustomerRequest request)
    {
        Customer customer = new Customer();

        customer.setId(UUID.randomUUID());
        customer.setName(request.name());
        customer.setAddress(request.address());
        customer.setCity(request.city());
        customer.setCountry(request.country());
        customer.setZipcode(request.zipcode());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setCreatedAt(LocalDateTime.now());
        customer.setActive(request.active());

        validator.validate(customer);

        Customer saved = repository.save(customer);

        return new CustomerResponse(
                saved.getId(),
                saved.getName(),
                saved.getAddress(),
                saved.getCity(),
                saved.getCountry(),
                saved.getZipcode(),
                saved.getPhone(),
                saved.getEmail(),
                saved.getCreatedAt(),
                saved.isActive()
        );
    }

    public CustomerResponse update(UUID id, UpdateCustomerRequest request) {

        Customer customer = repository.findById(id).orElseThrow(() -> new NotFoundException("Customer not found"));

        customer.setName(request.name());
        customer.setAddress(request.address());
        customer.setCity(request.city());
        customer.setCountry(request.country());
        customer.setZipcode(request.zipcode());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setActive(request.active());

        validator.validate(customer);

        var saved = repository.save(customer);

        return new CustomerResponse(
                saved.getId(),
                saved.getName(),
                saved.getAddress(),
                saved.getCity(),
                saved.getCountry(),
                saved.getZipcode(),
                saved.getPhone(),
                saved.getEmail(),
                saved.getCreatedAt(),
                saved.isActive()
        );
    }

    public void delete(DeleteCustomerRequest request)
    {
        Customer customer = repository.findById(request.id()).orElseThrow(() -> new NotFoundException("Customer not found"));
        repository.delete(customer);
    }
}