package com.timerbook.TimerBook.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timerbook.TimerBook.dto.LoginRequestDTO;
import com.timerbook.TimerBook.dto.RegisterRequestDTO;
import com.timerbook.TimerBook.dto.ResponseDTO;
import com.timerbook.TimerBook.models.Role;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.RoleRepository;
import com.timerbook.TimerBook.repository.UserRepository;
import com.timerbook.TimerBook.services.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Autenticacao e registro de usuarios")
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RoleRepository roleRepository;

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

    @PostMapping("/register")
        @Operation(
            summary = "Registra novo usuario",
            description = "Cria uma conta de usuario, atribui com ROLE_USER e retorna um token JWT."
        )
        @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "Usuario registrado com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Email ja cadastrado ou dados invalidos")
        })
        public ResponseEntity<ResponseDTO> register(
            @Parameter(
                name = "body",
                description = "Dados de cadastro: username, email e senha",
                required = true,
                schema = @Schema(implementation = RegisterRequestDTO.class)
            )
            @RequestBody RegisterRequestDTO body) {
        Optional<User> user = this.userRepository.findByEmail(body.email());
        
        if (user.isEmpty()) {
            User newUser = new User();

            newUser.setUsername(body.username());
            newUser.setEmail(body.email());
            newUser.setPassword(passwordEncoder.encode(body.password()));
            
            Role userRole = roleRepository.findByAuthority("ROLE_USER");

            if (userRole == null) {
                userRole = new Role(null, "ROLE_USER");
                roleRepository.save(userRole);
            }
            if (userRole != null) {
                newUser.getRoles().add(userRole);
            }

            this.userRepository.save(newUser);
            String token = this.tokenService.generateToken(newUser);

            return ResponseEntity.ok(new ResponseDTO(newUser.getUsername(), token));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
