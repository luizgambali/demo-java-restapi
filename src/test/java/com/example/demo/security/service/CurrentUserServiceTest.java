package com.example.demo.security.service;

import com.example.demo.exceptions.NotFoundException;
import com.example.demo.security.model.Role;
import com.example.demo.security.model.User;
import com.example.demo.security.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private CurrentUserService service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolvesLoggedUserFromAuthenticationName() {
        User user = user("maria", Role.USER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("maria", null));
        when(repository.findByUsername("maria")).thenReturn(Optional.of(user));

        assertEquals(user, service.get());
    }

    @Test
    void rejectsUnknownLoggedUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("maria", null));
        when(repository.findByUsername("maria")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.get());

        assertEquals("Logged user not found", exception.getMessage());
    }

    private User user(String username, Role role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setRole(role);
        user.setActive(true);
        return user;
    }
}
