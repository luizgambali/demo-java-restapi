package com.example.demo.security.controller;

import com.example.demo.security.dto.ChangePasswordRequest;
import com.example.demo.security.dto.RegisterRequest;
import com.example.demo.security.dto.UserResponse;
import com.example.demo.security.model.User;
import com.example.demo.security.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService userService) {
        this.service = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-all")
    public List<UserResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {

        UserResponse response = service.getById(id);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-admin")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {

        UserResponse user = service.createAdmin(request.username(), request.password());

        return ResponseEntity.status(201).body(user);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request){
        service.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {

        service.delete(id);

        return ResponseEntity.ok().build();
    }
}
