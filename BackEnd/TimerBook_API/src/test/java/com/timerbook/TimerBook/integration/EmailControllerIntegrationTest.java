package com.timerbook.TimerBook.integration;

import com.timerbook.TimerBook.dto.EmailRequestDTO;
import com.timerbook.TimerBook.models.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmailControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void sendEmailShouldRequireAuthenticationAndDelegateRequestBodyToService() throws Exception {
        User user = createEnabledUser("reader", "reader@mail.com", "secret123");
        ArgumentCaptor<EmailRequestDTO> captor = ArgumentCaptor.forClass(EmailRequestDTO.class);

        mockMvc.perform(post("/email/send")
                        .header("Authorization", bearerFor(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "to": "destino@mail.com",
                                  "subject": "Assunto",
                                  "message": "Mensagem"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Email sent successfully!"));

        verify(emailService).send(captor.capture());
        assertEquals("destino@mail.com", captor.getValue().getTo());
        assertEquals("Assunto", captor.getValue().getSubject());
        assertEquals("Mensagem", captor.getValue().getMessage());
    }

    @Test
    void sendEmailShouldRejectMissingToken() throws Exception {
        mockMvc.perform(post("/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "to": "destino@mail.com",
                                  "subject": "Assunto",
                                  "message": "Mensagem"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}
