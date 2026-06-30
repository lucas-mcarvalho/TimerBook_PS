package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.EmailRequestDTO;
import com.timerbook.TimerBook.models.PasswordResetToken;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.PasswordResetTokenRepository;
import com.timerbook.TimerBook.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordRecoveryService service;

    @Test
    void sendRecoveryEmailShouldCreateTokenAndSendEmailWithRecoveryLink() {
        User user = user();
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        ArgumentCaptor<EmailRequestDTO> emailCaptor = ArgumentCaptor.forClass(EmailRequestDTO.class);

        when(userRepository.findByEmail("reader@mail.com")).thenReturn(Optional.of(user));

        service.sendRecoveryEmail("reader@mail.com");

        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertNotNull(savedToken.getToken());
        assertSame(user, savedToken.getUser());
        assertTrue(savedToken.getExpiresAt().isAfter(LocalDateTime.now()));

        verify(emailService).send(emailCaptor.capture());
        EmailRequestDTO email = emailCaptor.getValue();
        assertEquals("reader@mail.com", email.getTo());
        assertEquals("Recuperação de Senha - TimerBook", email.getSubject());
        assertTrue(email.getMessage().contains("http://localhost:5173/redefinir-senha?token=" + savedToken.getToken()));
    }

    @Test
    void sendRecoveryEmailShouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@mail.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.sendRecoveryEmail("missing@mail.com"));

        assertEquals("Usuário não encontrado com este e-mail", exception.getMessage());
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).send(any());
    }

    @Test
    void validateTokenShouldAcceptNonExpiredToken() {
        PasswordResetToken token = token(LocalDateTime.now().plusMinutes(1));
        when(tokenRepository.findByToken("token")).thenReturn(Optional.of(token));

        assertDoesNotThrow(() -> service.validateToken("token"));
    }

    @Test
    void validateTokenShouldRejectMissingToken() {
        when(tokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.validateToken("missing"));

        assertEquals("Token inválido", exception.getMessage());
    }

    @Test
    void validateTokenShouldRejectExpiredToken() {
        PasswordResetToken token = token(LocalDateTime.now().minusSeconds(1));
        when(tokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.validateToken("expired"));

        assertEquals("Token expirado", exception.getMessage());
    }

    @Test
    void resetPasswordShouldEncodePasswordSaveUserAndDeleteToken() {
        User user = user();
        PasswordResetToken token = token(LocalDateTime.now().plusMinutes(1));
        token.setUser(user);

        when(tokenRepository.findByToken("token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");

        service.resetPassword("token", "new-password");

        assertEquals("encoded-new-password", user.getPassword());
        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
    }

    @Test
    void resetPasswordShouldRejectExpiredToken() {
        PasswordResetToken token = token(LocalDateTime.now().minusSeconds(1));
        token.setUser(user());
        when(tokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.resetPassword("expired", "new-password"));

        assertEquals("Token expirado", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("reader@mail.com");
        user.setPassword("old");
        return user;
    }

    private PasswordResetToken token(LocalDateTime expiresAt) {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("token");
        token.setExpiresAt(expiresAt);
        return token;
    }
}
