package com.timerbook.TimerBook.integration;

import com.timerbook.TimerBook.dto.EmailRequestDTO;
import com.timerbook.TimerBook.models.PasswordResetToken;
import com.timerbook.TimerBook.models.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PasswordRecoveryIntegrationTest extends AbstractIntegrationTest {

    @Test
    void recoveryFlowShouldCreateTokenSendEmailValidateTokenAndResetPassword() throws Exception {
        User user = createEnabledUser("reader", "reader@mail.com", "old-password");
        ArgumentCaptor<EmailRequestDTO> emailCaptor = ArgumentCaptor.forClass(EmailRequestDTO.class);

        mockMvc.perform(post("/forgot/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "reader@mail.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("E-mail de recuperação enviado!"));

        PasswordResetToken savedToken = tokenRepository.findAll().get(0);
        verify(emailService).send(emailCaptor.capture());
        assertTrue(emailCaptor.getValue().getMessage().contains(savedToken.getToken()));

        mockMvc.perform(get("/forgot/validate-token")
                        .param("token", savedToken.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token válido"));

        mockMvc.perform(post("/forgot/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s",
                                  "newPassword": "new-password"
                                }
                                """.formatted(savedToken.getToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha alterada com sucesso"));

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("new-password", updatedUser.getPassword()));
        assertTrue(tokenRepository.findByToken(savedToken.getToken()).isEmpty());
    }
}
