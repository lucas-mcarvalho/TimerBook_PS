package com.timerbook.TimerBook.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordValidatorServiceTest {

    private PasswordValidatorService service;

    @BeforeEach
    void setUp() {
        service = new PasswordValidatorService();
    }

    @Test
    void validateShouldAcceptPasswordThatMatchesAllRules() {
        assertDoesNotThrow(() -> service.validate("Timer@123"));
    }

    @Test
    void validateShouldRejectPasswordShorterThanMinimumLength() {
        assertInvalidPassword("Aa1@abc");
    }

    @Test
    void validateShouldRejectPasswordWithoutUppercaseLetter() {
        assertInvalidPassword("timer@123");
    }

    @Test
    void validateShouldRejectPasswordWithoutLowercaseLetter() {
        assertInvalidPassword("TIMER@123");
    }

    @Test
    void validateShouldRejectPasswordWithoutDigit() {
        assertInvalidPassword("Timer@abc");
    }

    @Test
    void validateShouldRejectPasswordWithoutSpecialCharacter() {
        assertInvalidPassword("Timer123");
    }

    @Test
    void validateShouldRejectPasswordWithWhitespace() {
        assertInvalidPassword("Timer @123");
    }

    private void assertInvalidPassword(String password) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.validate(password)
        );

        assertFalse(exception.getMessage().isBlank());
    }
}
