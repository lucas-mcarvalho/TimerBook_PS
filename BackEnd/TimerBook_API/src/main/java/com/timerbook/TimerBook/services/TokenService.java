package com.timerbook.TimerBook.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.timerbook.TimerBook.models.Role;
import com.timerbook.TimerBook.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            List<String> roles = user.getRoles().stream()
                    .map(Role::getAuthority)
                    .toList();

            return JWT.create()
                    .withIssuer("timerbook-login-api")
                    .withSubject(user.getEmail())
                    .withClaim("roles", roles)
                    .withExpiresAt(this.generateExpirationDate())
                    .sign(algorithm);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token", e);
        }
    }

    public DecodedJWT validateAndDecodeToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("timerbook-login-api")
                    .build()
                    .verify(token);
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    private Instant generateExpirationDate() {
        return Instant.now().plus(15, ChronoUnit.MINUTES);
    }

    public String createRefreshToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer("timerbook-login-api")
                    .withSubject(user.getEmail())
                    .withExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                    .sign(algorithm);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar refresh token", e);
        }
    }


    public String generateEmailVerificationToken(String email) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer("timerbook-email-verification")
                    .withSubject(email)
                    .withExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                    .sign(algorithm);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token de verificação de email", e);
        }
    }

    public DecodedJWT validateEmailToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                    .withIssuer("timerbook-email-verification")
                    .build()
                    .verify(token);
        } catch (JWTVerificationException e) {
            return null;
        }
    }

}
