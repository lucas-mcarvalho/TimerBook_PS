package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload mínimo para recuperação de senha")
public record EmailOnlyDTO(
        @Schema(description = "E-mail do usuário", example = "user@email.com")
        @Email
        @NotBlank
        String email
) {
}
