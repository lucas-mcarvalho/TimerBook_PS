package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.dto.LoginRequestDTO;
import com.timerbook.TimerBook.dto.RegisterRequestDTO;
import com.timerbook.TimerBook.dto.ResponseDTO;
import com.timerbook.TimerBook.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Autenticação e registro de usuários")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "Login do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(
            @RequestBody LoginRequestDTO body
    ) {
        try {
            ResponseDTO response = authService.login(body);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Registrar novo usuário com foto de perfil")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao registrar usuário")
    })
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO> register(

            @Parameter(description = "Nome do usuário", example = "João")
            @RequestParam("username") String username,

            @Parameter(description = "Email do usuário", example = "joao@email.com")
            @RequestParam("email") String email,

            @Parameter(description = "Senha do usuário", example = "123456")
            @RequestParam("password") String password,

            @Parameter(description = "Foto de perfil (opcional)")
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) {
        try {
            RegisterRequestDTO dto = new RegisterRequestDTO(username, email, password);
            ResponseDTO response = authService.register(dto, photo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Gerar novo access token usando refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renovado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Refresh token inválido ou expirado")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ResponseDTO> refreshToken(

            @Parameter(description = "Refresh token no formato Bearer",
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String refreshToken
    ) {
        try {
            ResponseDTO response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(403).build();
        }
    }
}