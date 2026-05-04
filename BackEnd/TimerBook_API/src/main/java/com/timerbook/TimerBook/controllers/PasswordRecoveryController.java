package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.controllers.docs.PasswordRecoveryDocs;
import com.timerbook.TimerBook.dto.EmailOnlyDTO;
import com.timerbook.TimerBook.dto.PasswordResponseDTO;
import com.timerbook.TimerBook.dto.ResetPasswordDTO;
import com.timerbook.TimerBook.services.PasswordRecoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forgot")
@Tag(name = "Password Recovery", description = "API para recuperação de senha")
public class PasswordRecoveryController implements PasswordRecoveryDocs {

    @Autowired
    private PasswordRecoveryService service;

    @GetMapping("/validate-token")
    @Operation(summary = "Validar token de recuperação de senha")
    public ResponseEntity<PasswordResponseDTO> validateToken(@RequestParam String token) {
        service.validateToken(token);
        return ResponseEntity.ok(new PasswordResponseDTO("Token válido"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Resetar senha com token válido")
    public ResponseEntity<PasswordResponseDTO> resetPassword(@Valid @RequestBody ResetPasswordDTO request) {
        service.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(new PasswordResponseDTO("Senha alterada com sucesso"));
    }

    @PostMapping("/request")
    @Operation(summary = "Solicitar recuperação de senha")
    public ResponseEntity<PasswordResponseDTO> requestRecovery(@Valid @RequestBody EmailOnlyDTO request) {
        service.sendRecoveryEmail(request.email());
        return ResponseEntity.ok(new PasswordResponseDTO("E-mail de recuperação enviado!"));
    }
}