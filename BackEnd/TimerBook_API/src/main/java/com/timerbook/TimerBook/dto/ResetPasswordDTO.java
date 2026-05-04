package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para redefinição de senha")
public record ResetPasswordDTO(
        @Schema(description = "Token de recuperação", example = "eyJhbGciOi...")
        @NotBlank
        String token,

        @Schema(description = "Nova senha", example = "senhaNova123")
        @NotBlank
        @Size(min = 6)
        String newPassword
) {}