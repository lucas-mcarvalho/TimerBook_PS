package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.controllers.docs.AuthControllerDocs;
import com.timerbook.TimerBook.dto.LoginRequestDTO;
import com.timerbook.TimerBook.dto.RegisterRequestDTO;
import com.timerbook.TimerBook.dto.ResponseDTO;
import com.timerbook.TimerBook.services.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Autenticação e registro de usuários")
public class AuthController  implements AuthControllerDocs{

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO body) {
        try {
            ResponseDTO response = authService.login(body);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> register(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) {
        try {
            RegisterRequestDTO dto = new RegisterRequestDTO(username, email, password, null);

            String message = authService.register(dto, photo);

            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        try {
            String message = authService.verifyEmailToken(token);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDTO> refreshToken(
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