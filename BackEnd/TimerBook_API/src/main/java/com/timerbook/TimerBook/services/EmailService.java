package com.timerbook.TimerBook.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.timerbook.TimerBook.config.EmailConfig;
import com.timerbook.TimerBook.dto.EmailRequestDTO;
import com.timerbook.TimerBook.mail.EmailSender;

import jakarta.mail.MessagingException;

@Service
public class EmailService {
    @Autowired
    private EmailSender emailSender;

    @Autowired
    private EmailConfig emailConfig;

    public void send(EmailRequestDTO emailRequest) {
        try {
            emailSender.to(emailRequest.getTo())
                    .withSubject(emailRequest.getSubject())
                    .withMessage(emailRequest.getMessage())
                    .send(emailConfig);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send e-mail", e);
        }
    }


    public void sendVerificationEmail(String toEmail, String link) {
        String subject = "TimerBook - Confirme sua conta";
        String message = "Olá!\n\n" +
                "Bem-vindo ao TimerBook. Para ativar sua conta e começar a gerenciar suas leituras, " +
                "por favor clique no link abaixo:\n\n" +
                link + "\n\n" +
                "Este link é válido por 24 horas.";
        EmailRequestDTO request = new EmailRequestDTO(toEmail, subject, message);
        this.send(request);
    }
}
