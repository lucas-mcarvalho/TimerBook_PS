package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.controllers.docs.PasswordRecoveryDocs;
import com.timerbook.TimerBook.dto.EmailOnlyDTO;
import com.timerbook.TimerBook.dto.PasswordResponseDTO;
import com.timerbook.TimerBook.dto.ResetPasswordDTO;
import com.timerbook.TimerBook.dto.ResponseDTO;
import com.timerbook.TimerBook.services.PasswordRecoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forgot")
public class PasswordRecoveryController implements PasswordRecoveryDocs {

    @Autowired
    private  PasswordRecoveryService service;

    @GetMapping("/validate-token")
    @Override
    public ResponseEntity<PasswordResponseDTO> validateToken(
            @RequestParam String token) {

        service.validateToken(token);
        return ResponseEntity.ok(new PasswordResponseDTO("Token válido"));
    }

    @PostMapping("/reset-password")
    @Override
    public ResponseEntity<PasswordResponseDTO> resetPassword(
            @RequestBody ResetPasswordDTO request) {

        service.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(new PasswordResponseDTO("Senha alterada com sucesso"));
    }


    @PostMapping("/request")
    public ResponseEntity<PasswordResponseDTO> requestRecovery(@RequestBody EmailOnlyDTO request) {
        service.sendRecoveryEmail(request.email());
        return ResponseEntity.ok(new PasswordResponseDTO("E-mail de recuperação enviado!"));
    }
}