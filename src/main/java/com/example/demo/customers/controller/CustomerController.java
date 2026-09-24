package com.example.demo.customers.controller;

import com.example.demo.customers.dto.CreateCustomerRequest;
import com.example.demo.customers.dto.CustomerResponse;
import com.example.demo.customers.dto.DeleteCustomerRequest;
import com.example.demo.customers.dto.UpdateCustomerRequest;
import com.example.demo.customers.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CustomerResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@RequestBody CreateCustomerRequest customer) {
        CustomerResponse saved = service.add(customer);
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable UUID id, @RequestBody UpdateCustomerRequest request) {
        CustomerResponse updated = service.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        DeleteCustomerRequest request = new DeleteCustomerRequest(id);
        service.delete(request);

        return ResponseEntity.ok().build();
    }
}