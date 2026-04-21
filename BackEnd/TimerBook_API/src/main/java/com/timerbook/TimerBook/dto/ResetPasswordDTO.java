package com.timerbook.TimerBook.dto;

public record ResetPasswordDTO(
        String token,
        String newPassword
) {}