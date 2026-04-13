package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.dto.UserDTO;
import com.timerbook.TimerBook.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.timerbook.TimerBook.dto.LoginRequestDTO;
import com.timerbook.TimerBook.dto.ResponseDTO;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.UserRepository;
import com.timerbook.TimerBook.services.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Autenticacao e registro de usuarios")
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
        @Operation(
            summary = "Realiza login",
            description = "Autentica um usuario por email e senha e retorna um token JWT para acesso as rotas protegidas."
        )
        @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "Login realizado com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Credenciais invalidas"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado")
        })
        public ResponseEntity<ResponseDTO> login(
            @Parameter(
                name = "body",
                description = "Credenciais de acesso: email e senha",
                required = true,
                schema = @Schema(implementation = LoginRequestDTO.class)
            )
            @RequestBody LoginRequestDTO body) {
        User user = this.userRepository.findByEmail(body.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (passwordEncoder.matches(body.password(), user.getPassword())) {
            String token = this.tokenService.generateToken(user);
            return ResponseEntity.ok(new ResponseDTO(user.getUsername(), token));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Registra novo usuário", description = "Cria usuário com foto de perfil opcional e retorna token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Email já cadastrado ou dados inválidos")
    })
    public ResponseEntity<ResponseDTO> register(
            @Parameter(description = "Nome do usuário", example = "João")
            @RequestPart("username") String username,

            @Parameter(description = "Email do usuário", example = "joao@email.com")
            @RequestPart("email") String email,

            @Parameter(description = "Senha do usuário", example = "123456")
            @RequestPart("password") String password,

            @Parameter(description = "Foto do usuário")
            @RequestPart(value = "photo", required = false) MultipartFile photo) {

        try {
            UserDTO dto = new UserDTO();
            dto.setUsername(username);
            dto.setEmail(email);
            dto.setPassword(password);
            dto.setPhotopath(photo);

            User newUser = userService.create(dto);
            String token = tokenService.generateToken(newUser);

            return ResponseEntity.ok(new ResponseDTO(newUser.getUsername(), token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
