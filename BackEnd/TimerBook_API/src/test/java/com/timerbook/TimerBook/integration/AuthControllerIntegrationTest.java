package com.timerbook.TimerBook.integration;

import com.timerbook.TimerBook.models.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void registerShouldPersistDisabledUserAndSendVerificationEmail() throws Exception {
        mockMvc.perform(multipart("/auth/register")
                        .param("username", "reader")
                        .param("email", "reader@mail.com")
                        .param("password", "Secret@123")
                        .param("dailyReadingGoalMinutes", "15"))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuário registrado com sucesso. Verifique seu e-mail para ativar a conta."));

        User savedUser = userRepository.findByEmail("reader@mail.com").orElseThrow();
        assertFalse(savedUser.getEnabled());
        assertEquals("reader", savedUser.getUsername());
        assertEquals(15, savedUser.getDailyReadingGoalMinutes());
        assertTrue(passwordEncoder.matches("Secret@123", savedUser.getPassword()));
        assertTrue(savedUser.getRoles().stream().anyMatch(role -> role.getAuthority().equals("ROLE_USER")));

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendVerificationEmail(org.mockito.ArgumentMatchers.eq("reader@mail.com"), linkCaptor.capture());
        assertTrue(linkCaptor.getValue().startsWith("http://localhost:5173/verify-email?token="));
    }

    @Test
    void registerShouldReturnBadRequestForDuplicatedEmail() throws Exception {
        createEnabledUser("reader", "reader@mail.com", "secret123");

        mockMvc.perform(multipart("/auth/register")
                        .param("username", "other")
                        .param("email", "reader@mail.com")
                        .param("password", "secret123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email já cadastrado!"));
    }

    @Test
    void verifyEmailShouldEnableUserWithValidToken() throws Exception {
        User user = createEnabledUser("reader", "reader@mail.com", "secret123");
        user.setEnabled(false);
        userRepository.saveAndFlush(user);
        String token = tokenService.generateEmailVerificationToken("reader@mail.com");

        mockMvc.perform(get("/auth/verify-email").param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string("E-mail verificado com sucesso!"));

        assertTrue(userRepository.findByEmail("reader@mail.com").orElseThrow().getEnabled());
    }

    @Test
    void loginShouldAuthenticateEnabledUserAndReturnTokens() throws Exception {
        User user = createEnabledUser("reader", "reader@mail.com", "secret123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "reader@mail.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("reader"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        assertNotNull(userRepository.findById(user.getId()).orElseThrow().getRefreshToken());
    }

    @Test
    void refreshShouldAcceptRefreshTokenWithoutRolesClaim() throws Exception {
        User user = createEnabledUser("reader", "reader@mail.com", "secret123");
        String refreshToken = tokenService.createRefreshToken(user);
        user.setRefreshToken(refreshToken);
        userRepository.saveAndFlush(user);

        mockMvc.perform(post("/auth/refresh")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("reader"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void refreshShouldReturnForbiddenForInvalidHeader() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .header("Authorization", "invalid"))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshShouldReturnForbiddenWhenRefreshTokenWasInvalidatedByLogout() throws Exception {
        User user = createEnabledUser("reader", "reader@mail.com", "secret123");
        String accessToken = tokenService.generateToken(user);
        String refreshToken = tokenService.createRefreshToken(user);
        user.setRefreshToken(refreshToken);
        userRepository.saveAndFlush(user);

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        assertNull(userRepository.findById(user.getId()).orElseThrow().getRefreshToken());

        mockMvc.perform(post("/auth/refresh")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutShouldReturnUnauthorizedForInvalidToken() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }
}
