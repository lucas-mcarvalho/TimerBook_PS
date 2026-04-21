package com.timerbook.TimerBook.services;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.timerbook.TimerBook.dto.LoginRequestDTO;
import com.timerbook.TimerBook.dto.RegisterRequestDTO;
import com.timerbook.TimerBook.dto.ResponseDTO;
import com.timerbook.TimerBook.models.Role;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.RoleRepository;
import com.timerbook.TimerBook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseDTO login(LoginRequestDTO body) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(body.email(), body.password())
        );

        User user = userRepository.findByEmail(body.email())
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        String accessToken = tokenService.generateToken(user);
        String refreshToken = tokenService.createRefreshToken(user);


        user.setRefreshToken(refreshToken);
        return new ResponseDTO(user.getUsername(), accessToken, refreshToken);
    }

    public ResponseDTO register(RegisterRequestDTO body, MultipartFile photo) {

        Optional<User> existingUser = userRepository.findByEmail(body.email());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email já cadastrado!");
        }

        User newUser = new User();
        newUser.setUsername(body.username());
        newUser.setEmail(body.email());
        newUser.setPassword(passwordEncoder.encode(body.password()));

        // 📸 salvar foto
        if (photo != null && !photo.isEmpty()) {
            String photoPath = fileStorageService.storeFile(photo, "profile");
            newUser.setPhotopath(photoPath);
        }

        Role userRole = roleRepository.findByAuthority("ROLE_USER");
        if (userRole == null) {
            userRole = new Role(null, "ROLE_USER");
            roleRepository.save(userRole);
        }
        newUser.getRoles().add(userRole);

        userRepository.save(newUser);

        String accessToken = tokenService.generateToken(newUser);
        String refreshToken = tokenService.createRefreshToken(newUser);

        newUser.setRefreshToken(refreshToken);
        userRepository.save(newUser);

        return new ResponseDTO(newUser.getUsername(), accessToken, refreshToken);
    }
    public ResponseDTO refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token inválido ou ausente");
        }
        String token = authHeader.replace("Bearer ", "");

        DecodedJWT decodedJWT = tokenService.validateAndDecodeToken(token);
        if (decodedJWT == null) {
            throw new RuntimeException("Refresh Token expirado ou inválido. Faça login novamente.");
        }

        String email = decodedJWT.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String newAccessToken = tokenService.generateToken(user);
        String newRefreshToken = tokenService.createRefreshToken(user);
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);
        return new ResponseDTO(user.getUsername(), newAccessToken, newRefreshToken);
    }
}