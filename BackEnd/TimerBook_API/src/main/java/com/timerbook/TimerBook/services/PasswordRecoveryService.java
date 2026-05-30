package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.EmailRequestDTO;
import com.timerbook.TimerBook.models.PasswordResetToken;
import com.timerbook.TimerBook.repository.PasswordResetTokenRepository;
import com.timerbook.TimerBook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordRecoveryService {

    @Autowired
    private  PasswordResetTokenRepository tokenRepository;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    public void sendRecoveryEmail(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com este e-mail"));

        String generatedToken = UUID.randomUUID().toString();

        PasswordResetToken tokenEntity = new PasswordResetToken();
        tokenEntity.setToken(generatedToken);
        tokenEntity.setUser(user);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusMinutes(2));

        tokenRepository.save(tokenEntity);
        String linkRecuperacao = "timerbook://reset-password?token=" + generatedToken;

        EmailRequestDTO emailRequest = new EmailRequestDTO();
        emailRequest.setTo(email);
        emailRequest.setSubject("Recuperação de Senha - TimerBook");
        emailRequest.setMessage("Olá! Você solicitou a recuperação de senha. \n\nclique no link e redefina a senha: "+linkRecuperacao);
        emailService.send(emailRequest);
    }



    public void validateToken(String token) {
        var tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }
    }

    public void resetPassword(String token, String newPassword) {

        var tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }
        var user = tokenEntity.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(tokenEntity);
    }
}
