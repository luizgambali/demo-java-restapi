package com.example.demo.security.service;

import com.example.demo.exceptions.BadRequestException;
import com.example.demo.security.dto.ChangePasswordRequest;
import com.example.demo.security.dto.LoginRequest;
import com.example.demo.security.dto.LoginResponse;
import com.example.demo.security.dto.UserResponse;
import com.example.demo.security.model.Role;
import com.example.demo.security.model.User;
import com.example.demo.security.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private UserRepository repository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService service;

    @Test
    void createsRegularUserWithEncodedPassword() {
        when(passwordEncoder.encode("senha-segura")).thenReturn("hash-da-senha");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = service.createUser("maria", "senha-segura");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("maria", saved.getUsername());
        assertEquals("hash-da-senha", saved.getPassword());
        assertEquals(Role.USER, saved.getRole());
        assertEquals(Role.USER, response.role());
    }

    @Test
    void authenticatesActiveUserAndReturnsJwt() {
        User user = user(UUID.randomUUID(), "maria", Role.USER, true, "hash-da-senha");
        when(repository.findByUsername("maria")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha-segura", "hash-da-senha")).thenReturn(true);
        when(tokenService.generateToken("maria", Role.USER)).thenReturn("jwt-token");

        LoginResponse response = service.login(new LoginRequest("maria", "senha-segura"));

        assertEquals(user.getId(), response.id());
        assertEquals("maria", response.username());
        assertEquals("jwt-token", response.token());
    }

    @Test
    void rejectsLoginWithInvalidPassword() {
        User user = user(UUID.randomUUID(), "maria", Role.USER, true, "hash-da-senha");
        when(repository.findByUsername("maria")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha-incorreta", "hash-da-senha")).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.login(new LoginRequest("maria", "senha-incorreta")));

        assertEquals("Invalid password", exception.getMessage());
        verify(tokenService, never()).generateToken(any(), any());
    }

    @Test
    void preventsRegularUserFromViewingAnotherUser() {
        User loggedUser = user(UUID.randomUUID(), "maria", Role.USER, true, "hash");
        User targetUser = user(UUID.randomUUID(), "joao", Role.USER, true, "hash");
        when(repository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(currentUserService.get()).thenReturn(loggedUser);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.getById(targetUser.getId()));

        assertEquals("Access denied", exception.getMessage());
    }

    @Test
    void allowsUserToChangeOwnPasswordAfterConfirmingOldPassword() {
        User user = user(UUID.randomUUID(), "maria", Role.USER, true, "hash-antigo");
        ChangePasswordRequest request = new ChangePasswordRequest(
                "maria", "senha-antiga", "senha-nova", "senha-nova");
        when(currentUserService.get()).thenReturn(user);
        when(repository.findByUsername("maria")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha-antiga", "hash-antigo")).thenReturn(true);
        when(passwordEncoder.encode("senha-nova")).thenReturn("hash-novo");

        service.changePassword(request);

        assertEquals("hash-novo", user.getPassword());
        verify(repository).save(user);
    }

    @Test
    void rejectsPasswordChangeWhenConfirmationDiffers() {
        User user = user(UUID.randomUUID(), "maria", Role.USER, true, "hash-antigo");
        ChangePasswordRequest request = new ChangePasswordRequest(
                "maria", "senha-antiga", "senha-nova", "outra-senha");
        when(currentUserService.get()).thenReturn(user);
        when(repository.findByUsername("maria")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha-antiga", "hash-antigo")).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.changePassword(request));

        assertEquals("Passwords do not match", exception.getMessage());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void preventsDeletingLastAdministrator() {
        User admin = user(UUID.randomUUID(), "admin", Role.ADMIN, true, "hash");
        when(repository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(repository.countByRole(Role.ADMIN)).thenReturn(1);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.delete(admin.getId()));

        assertEquals("The administrator cannot be deleted. There must be at least one administrator", exception.getMessage());
        verify(repository, never()).delete(any(User.class));
    }

    @Test
    void deletesUserWhenThereIsAnotherAdministrator() {
        User admin = user(UUID.randomUUID(), "admin", Role.ADMIN, true, "hash");
        when(repository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(repository.countByRole(Role.ADMIN)).thenReturn(2);

        service.delete(admin.getId());

        verify(repository).delete(admin);
    }

    private User user(UUID id, String username, Role role, boolean active, String password) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setActive(active);
        user.setPassword(password);
        return user;
    }
}
