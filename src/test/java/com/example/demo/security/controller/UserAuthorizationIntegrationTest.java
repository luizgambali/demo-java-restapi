package com.example.demo.security.controller;

import com.example.demo.security.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest(properties = {
        "jwt.secret=test-secret-with-at-least-thirty-two-bytes",
        "spring.datasource.url=jdbc:sqlite:file:method-security?mode=memory&cache=shared",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class UserAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void blocksRegularUserFromListingUsers() throws Exception {
        mockMvc.perform(get("/users/get-all").with(user("usuario").roles("USER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void allowsAdminToListUsers() throws Exception {
        when(userService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/users/get-all").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        verify(userService).findAll();
    }
}
