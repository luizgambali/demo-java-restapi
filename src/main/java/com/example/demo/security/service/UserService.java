package com.example.demo.security.service;

import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.security.dto.ChangePasswordRequest;
import com.example.demo.security.dto.LoginRequest;
import com.example.demo.security.dto.LoginResponse;
import com.example.demo.security.dto.UserResponse;
import com.example.demo.security.model.Role;
import com.example.demo.security.model.User;
import com.example.demo.security.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final CurrentUserService currentUserService;
    private final UserRepository repository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public UserService(CurrentUserService currentUserService,
                       UserRepository repository,
                       TokenService tokenService,
                       PasswordEncoder passwordEncoder) {

        this.currentUserService = currentUserService;
        this.repository = repository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(String username, String password) {

        return create(username, password, Role.USER);
    }

    public UserResponse createAdmin(String username, String password) {

        return create(username, password, Role.ADMIN);
    }

    public LoginResponse login(LoginRequest request) {

        User user = getValidUser(request.username(), request.password());

        String token = tokenService.generateToken(user.getUsername(), user.getRole());

        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                token
        );
    }

    public void changePassword(ChangePasswordRequest request)
    {
        User loggedUser = currentUserService.get();
        User targetUser = repository.findByUsername(request.username()).orElseThrow(() -> new NotFoundException("User not found"));

        if (loggedUser.getRole() == Role.USER)
        {
            if (!loggedUser.getId().equals(targetUser.getId())) {
                throw new BadRequestException("Access denied");
            }

            if (!passwordEncoder.matches(request.oldpassword(), targetUser.getPassword())) {
                throw new BadRequestException("Invalid password");
            }

            if (request.oldpassword().equals(request.newpassword())) {
                throw new BadRequestException("The new password must be different from the old password");
            }
        }

        if (!request.newpassword().equals(request.confirmpassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        targetUser.setPassword(passwordEncoder.encode(request.newpassword()));

        repository.save(targetUser);
    }

    public void delete(UUID userId){

        User user = repository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getRole() == Role.ADMIN)
        {
            int adminCount = repository.countByRole(Role.ADMIN);

            if (adminCount <= 1)
            {
                throw new BadRequestException("The administrator cannot be deleted. There must be at least one administrator");
            }
        }

        repository.delete(user);
    }

    public UserResponse getById(UUID userId) {

        User user = repository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        User loggedUser = currentUserService.get();

        if (loggedUser.getRole() == Role.USER){
            if (!userId.equals(loggedUser.getId()))
            {
                throw new BadRequestException("Access denied");
            }
        }

        return new UserResponse(user.getId(),user.getUsername(),user.isActive(),user.getRole());
    }
    public List<UserResponse> findAll() {
        User loggedUser = currentUserService.get();

        if (loggedUser.getRole() == Role.USER) {
            return List.of(
                    new UserResponse(
                            loggedUser.getId(),
                            loggedUser.getUsername(),
                            loggedUser.isActive(),
                            loggedUser.getRole()
                    )
            );
        }

        return repository.findAll()
            .stream()
            .map(user -> new UserResponse(
                user.getId(),
                user.getUsername(),
                user.isActive(),
                user.getRole()
            ))
            .toList();

    }

    private UserResponse create(String username, String password, Role role){
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);
        user.setRole(role);

        repository.save(user);

        return new UserResponse(user.getId(), user.getUsername(), user.isActive(), user.getRole());
    }

    private User getValidUser(String username, String password) {
        User user = repository.findByUsername(username).orElseThrow(() -> new NotFoundException("Invalid user or password"));

        if (!user.isActive()) {
            throw new BadRequestException("User is inactive");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid password");
        }

        return user;
    }
}