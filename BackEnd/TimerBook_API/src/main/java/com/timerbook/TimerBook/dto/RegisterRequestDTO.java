package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de usuário")
public record RegisterRequestDTO (
	@Schema(description = "Nome do usuário", example = "João Silva")
	@NotBlank
	String username,

	@Schema(description = "E-mail do usuário", example = "joao@email.com")
	@Email
	@NotBlank
	String email,

	@Schema(description = "Senha do usuário", example = "12345678")
	@NotBlank
	@Size(min = 6)
	String password,

	@Schema(description = "Caminho da foto de perfil")
	String photopath,

	@Schema(description = "Meta diária de leitura em minutos", example = "20")
	Integer dailyReadingGoalMinutes
){}
