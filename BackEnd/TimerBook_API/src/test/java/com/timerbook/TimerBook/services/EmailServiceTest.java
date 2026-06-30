package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.config.EmailConfig;
import com.timerbook.TimerBook.dto.EmailRequestDTO;
import com.timerbook.TimerBook.mail.EmailSender;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private EmailSender emailSender;

    @Mock
    private EmailConfig emailConfig;

    @InjectMocks
    private EmailService service;

    @Test
    void sendShouldDelegateToEmailSenderFluentApi() throws Exception {
        EmailRequestDTO request = new EmailRequestDTO("reader@mail.com", "Assunto", "Mensagem");
        mockEmailSenderChain(request.getTo(), request.getSubject(), request.getMessage());

        service.send(request);

        verify(emailSender).to("reader@mail.com");
        verify(emailSender).withSubject("Assunto");
        verify(emailSender).withMessage("Mensagem");
        verify(emailSender).send(emailConfig);
    }

    @Test
    void sendShouldWrapMessagingException() throws Exception {
        EmailRequestDTO request = new EmailRequestDTO("reader@mail.com", "Assunto", "Mensagem");
        mockEmailSenderChain(request.getTo(), request.getSubject(), request.getMessage());
        doThrow(new MessagingException("smtp down")).when(emailSender).send(emailConfig);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.send(request));

        assertEquals("Failed to send e-mail", exception.getMessage());
        assertInstanceOf(MessagingException.class, exception.getCause());
    }

    @Test
    void sendVerificationEmailShouldBuildExpectedSubjectAndLinkMessage() throws Exception {
        when(emailSender.to("reader@mail.com")).thenReturn(emailSender);
        when(emailSender.withSubject(anyString())).thenReturn(emailSender);
        when(emailSender.withMessage(anyString())).thenReturn(emailSender);

        service.sendVerificationEmail("reader@mail.com", "http://localhost/verify?token=abc");

        verify(emailSender).withSubject("TimerBook - Confirme sua conta");
        verify(emailSender).withMessage(argThat(message ->
                message.contains("Bem-vindo ao TimerBook")
                        && message.contains("http://localhost/verify?token=abc")
                        && message.contains("Este link é válido por 24 horas.")
        ));
        verify(emailSender).send(emailConfig);
    }

    @Test
    void sendReadingReminderEmailShouldEscapeUsernameInHtmlMessage() throws Exception {
        when(emailSender.to("reader@mail.com")).thenReturn(emailSender);
        when(emailSender.withSubject(anyString())).thenReturn(emailSender);
        when(emailSender.withMessage(anyString())).thenReturn(emailSender);

        service.sendReadingReminderEmail("reader@mail.com", "<Lucas & 'Dev'>");

        verify(emailSender).withSubject("TimerBook - Hora de voltar a ler");
        verify(emailSender).withMessage(argThat(message ->
                message.contains("&lt;Lucas &amp; &#39;Dev&#39;&gt;")
                        && message.contains("faz algum tempo desde sua última leitura")
        ));
        verify(emailSender).send(emailConfig);
    }

    @Test
    void sendReadingReminderEmailShouldUseGenericGreetingForBlankUsername() throws Exception {
        when(emailSender.to("reader@mail.com")).thenReturn(emailSender);
        when(emailSender.withSubject(anyString())).thenReturn(emailSender);
        when(emailSender.withMessage(anyString())).thenReturn(emailSender);

        service.sendReadingReminderEmail("reader@mail.com", " ");

        verify(emailSender).withMessage(argThat(message -> message.contains("<p>Olá!</p>")));
    }

    private void mockEmailSenderChain(String to, String subject, String message) throws Exception {
        when(emailSender.to(to)).thenReturn(emailSender);
        when(emailSender.withSubject(subject)).thenReturn(emailSender);
        when(emailSender.withMessage(message)).thenReturn(emailSender);
    }
}
