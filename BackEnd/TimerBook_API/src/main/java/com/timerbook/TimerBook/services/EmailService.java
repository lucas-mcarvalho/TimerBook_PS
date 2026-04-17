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

    public void sendEmail(EmailRequestDTO emailRequest) {
        try {
            emailSender.to(emailRequest.getTo())
                    .withSubject(emailRequest.getSubject())
                    .withMessage(emailRequest.getMessage())
                    .send(emailConfig);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send e-mail", e);
        }
    }
}
