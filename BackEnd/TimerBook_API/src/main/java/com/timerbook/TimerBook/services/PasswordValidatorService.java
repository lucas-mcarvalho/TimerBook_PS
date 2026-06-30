package com.timerbook.TimerBook.services;

import org.passay.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class PasswordValidatorService {

    private final PasswordValidator validator = new PasswordValidator(Arrays.asList(
        new LengthRule(8, 64),
        new CharacterRule(EnglishCharacterData.UpperCase, 1),
        new CharacterRule(EnglishCharacterData.LowerCase, 1),
        new CharacterRule(EnglishCharacterData.Digit, 1),
        new CharacterRule(EnglishCharacterData.Special, 1),
        new WhitespaceRule()
    ));

    public void validate(String password) {
        RuleResult result = validator.validate(new PasswordData(password));

        if (!result.isValid()) {
            throw new IllegalArgumentException(
                String.join(", ", validator.getMessages(result))
            );
        }
    }
}
