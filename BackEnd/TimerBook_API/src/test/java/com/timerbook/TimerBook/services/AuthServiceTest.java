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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AchievementService achievementService;

    @Mock
    private UserService userService;

    @Mock
    private PasswordValidatorService passwordValidatorService;

    @InjectMocks
    private AuthService service;

    @Test
    void loginShouldAuthenticateGenerateTokensPersistRefreshTokenAndReturnAchievements() {
        User user = user("reader", "reader@mail.com");
        List<AchievementDTO> achievements = List.of(new AchievementDTO("Login", "login.svg", "Primeiro login"));

        when(userRepository.findByEmail("reader@mail.com")).thenReturn(Optional.of(user));
        when(tokenService.generateToken(user)).thenReturn("access-token");
        when(tokenService.createRefreshToken(user)).thenReturn("refresh-token");
        when(achievementService.checkFirstLogin(user)).thenReturn(achievements);

        ResponseDTO response = service.login(new LoginRequestDTO("reader@mail.com", "secret"));

        assertEquals("reader", response.username());
        assertEquals("access-token", response.token());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals(achievements, response.novasConquistas());
        assertEquals("refresh-token", user.getRefreshToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).save(user);
    }

    @Test
    void loginShouldThrowWhenAuthenticatedUserIsNotFound() {
        when(userRepository.findByEmail("missing@mail.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.login(new LoginRequestDTO("missing@mail.com", "secret"))
        );

        assertEquals("Usuario nao encontrado", exception.getMessage());
    }

    @Test
    void registerShouldCreateDisabledUserStorePhotoAssignRoleAndSendVerificationEmail() {
        RegisterRequestDTO body = new RegisterRequestDTO("reader", "reader@mail.com", "Secret@123", null, 15);
        MockMultipartFile photo = new MockMultipartFile("photo", "avatar.png", "image/png", "img".getBytes());
        Role role = new Role(1L, "ROLE_USER");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findByEmail(body.email())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(body.username())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(body.password())).thenReturn("encoded-secret");
        when(userService.normalizeReadingGoal(15)).thenReturn(15);
        when(fileStorageService.storeFile(photo, "profile")).thenReturn("uploads/profile/avatar.png");
        when(roleRepository.findByAuthority("ROLE_USER")).thenReturn(role);
        when(tokenService.generateEmailVerificationToken(body.email())).thenReturn("email-token");

        String result = service.register(body, photo);

        assertEquals("Usuário registrado com sucesso. Verifique seu e-mail para ativar a conta.", result);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertFalse(savedUser.getEnabled());
        assertEquals("reader", savedUser.getUsername());
        assertEquals("reader@mail.com", savedUser.getEmail());
        assertEquals("encoded-secret", savedUser.getPassword());
        assertEquals("uploads/profile/avatar.png", savedUser.getPhotopath());
        assertEquals(15, savedUser.getDailyReadingGoalMinutes());
        assertTrue(savedUser.getRoles().contains(role));
        verify(passwordValidatorService).validate("Secret@123");
        verify(emailService).sendVerificationEmail(
                "reader@mail.com",
                "http://localhost:5173/verify-email?token=email-token"
        );
    }

    @Test
    void registerShouldCreateUserRoleWhenItDoesNotExist() {
        RegisterRequestDTO body = new RegisterRequestDTO("reader", "reader@mail.com", "Secret@123", null, null);

        when(userRepository.findByEmail(body.email())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(body.username())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(body.password())).thenReturn("encoded-secret");
        when(userService.normalizeReadingGoal(null)).thenReturn(User.DEFAULT_DAILY_READING_GOAL_MINUTES);
        when(roleRepository.findByAuthority("ROLE_USER")).thenReturn(null);
        when(tokenService.generateEmailVerificationToken(body.email())).thenReturn("email-token");

        service.register(body, null);

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        assertEquals("ROLE_USER", roleCaptor.getValue().getAuthority());
        verify(userRepository).save(argThat(user -> user.getRoles().contains(roleCaptor.getValue())));
        verify(passwordValidatorService).validate("Secret@123");
    }

    @Test
    void registerShouldRejectDuplicatedEmail() {
        RegisterRequestDTO body = new RegisterRequestDTO("reader", "reader@mail.com", "Secret@123", null, 10);
        when(userRepository.findByEmail(body.email())).thenReturn(Optional.of(new User()));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.register(body, null));

        assertEquals("Email já cadastrado!", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(passwordValidatorService, never()).validate(anyString());
    }

    @Test
    void registerShouldRejectDuplicatedUsername() {
        RegisterRequestDTO body = new RegisterRequestDTO("reader", "reader@mail.com", "Secret@123", null, 10);
        when(userRepository.findByEmail(body.email())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(body.username())).thenReturn(Optional.of(new User()));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.register(body, null));

        assertEquals("Este username já está em uso!", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(passwordValidatorService, never()).validate(anyString());
    }

    @Test
    void registerShouldRejectInvalidPasswordBeforeEncodingOrSavingUser() {
        RegisterRequestDTO body = new RegisterRequestDTO("reader", "reader@mail.com", "weak", null, 10);

        when(userRepository.findByEmail(body.email())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(body.username())).thenReturn(Optional.empty());
        doThrow(new IllegalArgumentException("Senha inválida")).when(passwordValidatorService).validate(body.password());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.register(body, null));

        assertEquals("Senha inválida", exception.getMessage());
        verify(passwordValidatorService).validate("weak");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void verifyEmailTokenShouldEnableUser() {
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        User user = user("reader", "reader@mail.com");
        user.setEnabled(false);

        when(tokenService.validateEmailToken("token")).thenReturn(decodedJWT);
        when(decodedJWT.getSubject()).thenReturn("reader@mail.com");
        when(userRepository.findByEmail("reader@mail.com")).thenReturn(Optional.of(user));

        String result = service.verifyEmailToken("token");

        assertEquals("E-mail verificado com sucesso!", result);
        assertTrue(user.getEnabled());
        verify(userRepository).save(user);
    }

    @Test
    void verifyEmailTokenShouldReturnMessageWhenUserIsAlreadyEnabled() {
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        User user = user("reader", "reader@mail.com");
        user.setEnabled(true);

        when(tokenService.validateEmailToken("token")).thenReturn(decodedJWT);
        when(decodedJWT.getSubject()).thenReturn("reader@mail.com");
        when(userRepository.findByEmail("reader@mail.com")).thenReturn(Optional.of(user));

        String result = service.verifyEmailToken("token");

        assertEquals("Sua conta já está ativada! Você pode fazer o login.", result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyEmailTokenShouldRejectInvalidToken() {
        when(tokenService.validateEmailToken("token")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.verifyEmailToken("token"));

        assertEquals("Link de verificação inválido ou expirado.", exception.getMessage());
    }

    @Test
    void verifyEmailTokenShouldThrowWhenUserDoesNotExist() {
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(tokenService.validateEmailToken("token")).thenReturn(decodedJWT);
        when(decodedJWT.getSubject()).thenReturn("missing@mail.com");
        when(userRepository.findByEmail("missing@mail.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.verifyEmailToken("token"));

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void refreshTokenShouldGenerateNewTokensAndPersistRefreshToken() {
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        User user = user("reader", "reader@mail.com");

        when(tokenService.validateAndDecodeToken("old-refresh")).thenReturn(decodedJWT);
        when(decodedJWT.getSubject()).thenReturn("reader@mail.com");
        user.setRefreshToken("old-refresh");
        when(userRepository.findByEmail("reader@mail.com")).thenReturn(Optional.of(user));
        when(tokenService.generateToken(user)).thenReturn("new-access");
        when(tokenService.createRefreshToken(user)).thenReturn("new-refresh");

        ResponseDTO response = service.refreshToken("Bearer old-refresh");

        assertEquals("reader", response.username());
        assertEquals("new-access", response.token());
        assertEquals("new-refresh", response.refreshToken());
        assertNull(response.novasConquistas());
        assertEquals("new-refresh", user.getRefreshToken());
        verify(userRepository).save(user);
    }

    @Test
    void refreshTokenShouldRejectMissingBearerHeader() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.refreshToken("old-refresh"));
        assertEquals("Token inválido ou ausente", exception.getMessage());
    }

    @Test
    void refreshTokenShouldRejectInvalidDecodedToken() {
        when(tokenService.validateAndDecodeToken("old-refresh")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.refreshToken("Bearer old-refresh"));

        assertEquals("Refresh Token expirado ou inválido. Faça login novamente.", exception.getMessage());
    }

    @Test
    void refreshTokenShouldRejectRefreshTokenThatDoesNotMatchStoredToken() {
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        User user = user("reader", "reader@mail.com");
        user.setRefreshToken("stored-refresh");

        when(tokenService.validateAndDecodeToken("old-refresh")).thenReturn(decodedJWT);
        when(decodedJWT.getSubject()).thenReturn("reader@mail.com");
        when(userRepository.findByEmail("reader@mail.com")).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.refreshToken("Bearer old-refresh"));

        assertEquals("Refresh Token expirado ou inválido. Faça login novamente.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void logoutShouldClearStoredRefreshToken() {
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        User user = user("reader", "reader@mail.com");
        user.setRefreshToken("refresh-token");

        when(tokenService.validateAndDecodeToken("access-token")).thenReturn(decodedJWT);
        when(decodedJWT.getSubject()).thenReturn("reader@mail.com");
        when(userRepository.findByEmail("reader@mail.com")).thenReturn(Optional.of(user));

        service.logout("Bearer access-token");

        assertNull(user.getRefreshToken());
        verify(userRepository).save(user);
    }

    @Test
    void logoutShouldRejectInvalidToken() {
        when(tokenService.validateAndDecodeToken("access-token")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.logout("Bearer access-token"));

        assertEquals("Token inválido", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    private User user(String username, String email) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("encoded");
        return user;
    }
}
