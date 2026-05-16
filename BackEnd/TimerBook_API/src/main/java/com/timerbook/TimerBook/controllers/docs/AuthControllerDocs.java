package com.timerbook.TimerBook.controllers.docs;


import com.timerbook.TimerBook.dto.LoginRequestDTO;
import com.timerbook.TimerBook.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Auth", description = "Autenticação e registro de usuários")

public interface AuthControllerDocs {

    @Operation(summary = "Login do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Credenciais inválidas")
    })
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDTO body
    );

    @Operation(summary = "Cadastro de usuário")
    @Parameter(description = "Nome do usuário", example = "João")
    @Parameter(description = "Senha do usuário", example = "123456")
    @Parameter(description = "Foto de perfil (opcional)")
    ResponseEntity<String> register(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @Parameter(description = "Meta diária de leitura em minutos (valores permitidos: 5, 10, 15, 30, 60)", example = "15")
            @RequestParam(value = "dailyReadingGoalMinutes", required = false) Integer dailyReadingGoalMinutes,
            MultipartFile photo
    );
    @Operation(summary = "Gerar novo access token usando refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renovado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Refresh token inválido ou expirado")
    })
    public ResponseEntity<ResponseDTO> refreshToken(

            @Parameter(description = "Refresh token no formato Bearer",
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String refreshToken

    );

    @Operation(summary = "Logout do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou ausente")
    })
    ResponseEntity<Void> logout(
            @Parameter(description = "Access token no formato Bearer",
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Verificar e-mail do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "E-mail verificado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    })
    ResponseEntity<String> verifyEmail(
            @Parameter(description = "Token enviado por e-mail")
            @RequestParam("token") String token
    );
}
