package com.example.demo.security.service;

import com.example.demo.security.model.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenServiceTest {

    private static final String SECRET = "test-secret-with-at-least-thirty-two-bytes";
    private final TokenService tokenService = new TokenService(SECRET);

    @Test
    void generatesValidTokenWithSubjectAndRole() {
        String token = tokenService.generateToken("maria", Role.ADMIN);

        assertTrue(tokenService.isValid(token));
        assertEquals("maria", tokenService.extractUsername(token));
        assertEquals("ADMIN", tokenService.extractRole(token));
    }

    @Test
    void rejectsInvalidToken() {
        assertFalse(tokenService.isValid("not-a-jwt"));
    }
}
