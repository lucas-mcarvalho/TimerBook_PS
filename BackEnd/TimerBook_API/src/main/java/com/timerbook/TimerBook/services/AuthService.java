package com.timerbook.TimerBook.services;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.timerbook.TimerBook.dto.AchievementDTO;
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

import java.util.List;
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
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordValidatorService passwordValidatorService;

    public ResponseDTO login(LoginRequestDTO body) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(body.email(), body.password())
        );

        User user = userRepository.findByEmail(body.email())
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        String accessToken = tokenService.generateToken(user);
        String refreshToken = tokenService.createRefreshToken(user);


        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        List<AchievementDTO> conquistas = achievementService.checkFirstLogin(user);
        return new ResponseDTO(user.getUsername(), accessToken, refreshToken,conquistas);
    }

    public String register(RegisterRequestDTO body, MultipartFile photo) {

        Optional<User> existingUser = userRepository.findByEmail(body.email());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email já cadastrado!");
        }

        Optional<User> existingUsername = userRepository.findByUsername(body.username());
        if (existingUsername.isPresent()) {
            throw new RuntimeException("Este username já está em uso!");
        }

        passwordValidatorService.validate(body.password());

        User newUser = new User();
        newUser.setEnabled(false);
        newUser.setUsername(body.username());
        newUser.setEmail(body.email());
        newUser.setPassword(passwordEncoder.encode(body.password()));
        newUser.setDailyReadingGoalMinutes(userService.normalizeReadingGoal(body.dailyReadingGoalMinutes()));

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
        String emailToken = tokenService.generateEmailVerificationToken(newUser.getEmail());
        String link = "http://localhost:5173/verify-email?token=" + emailToken;

        emailService.sendVerificationEmail(newUser.getEmail(), link);
        return "Usuário registrado com sucesso. Verifique seu e-mail para ativar a conta.";
    }


    public String verifyEmailToken(String token) {
        DecodedJWT decodedJWT = tokenService.validateEmailToken(token);
        if (decodedJWT == null) {
            throw new RuntimeException("Link de verificação inválido ou expirado.");
        }
        String email = decodedJWT.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (user.getEnabled()) {
            return "Sua conta já está ativada! Você pode fazer o login.";
        }

        user.setEnabled(true);
        userRepository.save(user);

        return "E-mail verificado com sucesso!";
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

        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(token)) {
            throw new RuntimeException("Refresh Token expirado ou inválido. Faça login novamente.");
        }

        String newAccessToken = tokenService.generateToken(user);
        String newRefreshToken = tokenService.createRefreshToken(user);
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);
        return new ResponseDTO(user.getUsername(), newAccessToken, newRefreshToken,null);
    }

    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token inválido ou ausente");
        }
        String token = authHeader.replace("Bearer ", "");

        DecodedJWT decodedJWT = tokenService.validateAndDecodeToken(token);
        if (decodedJWT == null) {
            throw new RuntimeException("Token inválido");
        }

        String email = decodedJWT.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.setRefreshToken(null);
        userRepository.save(user);
    }
}
