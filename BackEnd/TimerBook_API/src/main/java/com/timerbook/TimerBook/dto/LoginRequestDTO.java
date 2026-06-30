package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de login")
public record LoginRequestDTO(
	@Schema(description = "E-mail ou username do usuário", example = "user@email.com ou joao123")
	@NotBlank
	String email,

	@Schema(description = "Senha do usuário", example = "senha-segura")
	@NotBlank
	String password
) {}
