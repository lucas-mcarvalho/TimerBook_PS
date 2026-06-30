package com.timerbook.TimerBook.services;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.timerbook.TimerBook.models.Role;
import com.timerbook.TimerBook.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService service;

    @BeforeEach
    void setUp() {
        service = new TokenService();
        ReflectionTestUtils.setField(service, "secret", "unit-test-secret");
    }

    @Test
    void generateTokenShouldCreateValidAccessTokenWithSubjectAndRoles() {
        User user = user();

        String token = service.generateToken(user);
        DecodedJWT decoded = service.validateAndDecodeToken(token);

        assertNotNull(decoded);
        assertEquals("timerbook-login-api", decoded.getIssuer());
        assertEquals("reader@mail.com", decoded.getSubject());
        assertEquals(List.of("ROLE_USER"), decoded.getClaim("roles").asList(String.class));
        assertNotNull(decoded.getExpiresAt());
    }

    @Test
    void createRefreshTokenShouldCreateValidLoginTokenWithoutRolesClaim() {
        User user = user();

        String token = service.createRefreshToken(user);
        DecodedJWT decoded = service.validateAndDecodeToken(token);

        assertNotNull(decoded);
        assertEquals("timerbook-login-api", decoded.getIssuer());
        assertEquals("reader@mail.com", decoded.getSubject());
        assertTrue(decoded.getClaim("roles").isMissing());
    }

    @Test
    void generateEmailVerificationTokenShouldValidateWithEmailIssuer() {
        String token = service.generateEmailVerificationToken("reader@mail.com");

        DecodedJWT decoded = service.validateEmailToken(token);

        assertNotNull(decoded);
        assertEquals("timerbook-email-verification", decoded.getIssuer());
        assertEquals("reader@mail.com", decoded.getSubject());
        assertNotNull(decoded.getExpiresAt());
    }

    @Test
    void validateAndDecodeTokenShouldReturnNullForInvalidToken() {
        assertNull(service.validateAndDecodeToken("not-a-jwt"));
    }

    @Test
    void validateEmailTokenShouldReturnNullWhenIssuerDoesNotMatch() {
        String loginToken = service.generateToken(user());

        assertNull(service.validateEmailToken(loginToken));
    }

    @Test
    void generateTokenShouldWrapUnexpectedFailures() {
        ReflectionTestUtils.setField(service, "secret", null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.generateToken(user()));

        assertEquals("Erro ao gerar token", exception.getMessage());
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setUsername("reader");
        user.setEmail("reader@mail.com");
        user.getRoles().add(new Role(1L, "ROLE_USER"));
        return user;
    }
}
