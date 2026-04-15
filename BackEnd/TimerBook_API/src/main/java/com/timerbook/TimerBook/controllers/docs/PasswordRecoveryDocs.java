package com.timerbook.TimerBook.controllers.docs;

import com.timerbook.TimerBook.dto.EmailOnlyDTO;
import com.timerbook.TimerBook.dto.PasswordResponseDTO;
import com.timerbook.TimerBook.dto.ResetPasswordDTO;
import com.timerbook.TimerBook.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface PasswordRecoveryDocs {

    @Operation(summary = "Validar token de recuperação")
    @ApiResponse(responseCode = "200", description = "Token válido")
    @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    ResponseEntity<PasswordResponseDTO> validateToken(String token);

    @Operation(summary = "Redefinir senha")
    @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso")
    @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    ResponseEntity<PasswordResponseDTO> resetPassword(ResetPasswordDTO request);

    @Operation(summary = "Solicitar recuperação de senha (envia e-mail com token)")
    @ApiResponse(responseCode = "200", description = "E-mail de recuperação enviado!")
    @ApiResponse(responseCode = "400", description = "Usuário não encontrado com este e-mail")
    ResponseEntity<PasswordResponseDTO> requestRecovery(@RequestBody EmailOnlyDTO email);
}