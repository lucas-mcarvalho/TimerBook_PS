package com.timerbook.TimerBook.services;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.timerbook.TimerBook.dto.UserDTO;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserService service;

    @Test
    void createShouldSaveUserWithEncodedPasswordRoleAndDefaultGoal() {
        UserDTO dto = userDto("teste", "teste@gmail.com", "123456");
        Role role = new Role(1L, "ROLE_USER");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");
        when(roleRepository.findByAuthority("ROLE_USER")).thenReturn(role);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        User result = service.create(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("teste", result.getUsername());
        assertEquals("teste@gmail.com", result.getEmail());
        assertEquals("encoded", result.getPassword());
        assertEquals(User.DEFAULT_DAILY_READING_GOAL_MINUTES, result.getDailyReadingGoalMinutes());
        assertTrue(result.getRoles().contains(role));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createShouldStorePhotoAndUseProvidedGoal() {
        MockMultipartFile photo = new MockMultipartFile("photo", "me.png", "image/png", "img".getBytes());
        UserDTO dto = userDto("teste", "teste@gmail.com", "123456");
        dto.setPhotopath(photo);
        dto.setDailyReadingGoalMinutes(20);

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");
        when(fileStorageService.storeFile(photo, "users")).thenReturn("uploads/users/me.png");
        when(roleRepository.findByAuthority("ROLE_USER")).thenReturn(new Role(1L, "ROLE_USER"));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.create(dto);

        assertEquals("uploads/users/me.png", result.getPhotopath());
        assertEquals(20, result.getDailyReadingGoalMinutes());
        verify(fileStorageService).storeFile(photo, "users");
    }

    @Test
    void createShouldRejectDuplicatedEmail() {
        UserDTO dto = userDto("teste", "teste@gmail.com", "123456");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        assertEquals("Email já cadastrado", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createShouldRejectDuplicatedUsername() {
        UserDTO dto = userDto("teste", "teste@gmail.com", "123456");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        assertEquals("Este username já está em uso", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createShouldFailWhenUserRoleDoesNotExist() {
        UserDTO dto = userDto("teste", "teste@gmail.com", "123456");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");
        when(roleRepository.findByAuthority("ROLE_USER")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.create(dto));
        assertEquals("ROLE_USER não encontrada. Verifique se as migrations foram executadas.", exception.getMessage());
    }

    @Test
    void updateShouldChangeUsernameAndEmail() {
        User user = user(1L, "old", "old@gmail.com");
        UserDTO dto = userDto("new", "new@gmail.com", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("new")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.update(1L, dto);

        assertEquals("new", result.getUsername());
        assertEquals("new@gmail.com", result.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void updateShouldRejectUsernameUsedByAnotherUser() {
        User user = user(1L, "old", "old@gmail.com");
        UserDTO dto = userDto("taken", "old@gmail.com", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("taken")).thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.update(1L, dto));
        assertEquals("Este username já está em uso por outro usuário", exception.getMessage());
    }

    @Test
    void updateShouldReplacePhoto() {
        User user = user(1L, "teste", "teste@gmail.com");
        user.setPhotopath("uploads/users/old.png");
        MockMultipartFile newPhoto = new MockMultipartFile("photo", "new.png", "image/png", "img".getBytes());
        UserDTO dto = userDto("teste", "teste@gmail.com", null);
        dto.setPhotopath(newPhoto);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(fileStorageService.storeFile(newPhoto, "users")).thenReturn("uploads/users/new.png");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.update(1L, dto);

        assertEquals("uploads/users/new.png", result.getPhotopath());
        verify(fileStorageService).deleteFile("uploads/users/old.png");
        verify(fileStorageService).storeFile(newPhoto, "users");
    }

    @Test
    void updateShouldRemovePhoto() {
        User user = user(1L, "teste", "teste@gmail.com");
        user.setPhotopath("uploads/users/old.png");
        UserDTO dto = userDto("teste", "teste@gmail.com", null);
        dto.setRemovePhoto(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.update(1L, dto);

        assertNull(result.getPhotopath());
        verify(fileStorageService).deleteFile("uploads/users/old.png");
    }

    @Test
    void updateShouldThrowWhenUserDoesNotExist() {
        UserDTO dto = userDto("new", "new@gmail.com", null);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.update(1L, dto));
        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void deleteShouldRemovePhotoClearRolesAndDeleteUser() {
        User user = user(1L, "teste", "teste@gmail.com");
        user.setPhotopath("uploads/users/photo.png");
        user.getRoles().add(new Role(1L, "ROLE_USER"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        service.delete(1L);

        assertTrue(user.getRoles().isEmpty());
        verify(fileStorageService).deleteFile("uploads/users/photo.png");
        verify(userRepository).delete(user);
    }

    @Test
    void findByIdShouldReturnUser() {
        User user = user(1L, "teste", "teste@gmail.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = service.findById(1L);

        assertEquals(user, result);
    }

    @Test
    void findByIdShouldThrowWhenMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.findById(1L));
        assertEquals("Usuário não encontrado com id: 1", exception.getMessage());
    }

    @Test
    void getMeShouldReturnUserFromBearerToken() {
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        User user = user(1L, "teste", "teste@gmail.com");

        when(tokenService.validateAndDecodeToken("token")).thenReturn(decodedJWT);
        when(decodedJWT.getSubject()).thenReturn("teste@gmail.com");
        when(userRepository.findByEmail("teste@gmail.com")).thenReturn(Optional.of(user));

        User result = service.getMe("Bearer token");

        assertEquals(user, result);
    }

    @Test
    void getMeShouldRejectMissingBearerToken() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.getMe("token"));
        assertEquals("Token inválido ou ausente", exception.getMessage());
    }

    @Test
    void getMeShouldRejectInvalidDecodedToken() {
        when(tokenService.validateAndDecodeToken("token")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.getMe("Bearer token"));
        assertEquals("Token inválido", exception.getMessage());
    }

    @Test
    void getMyReadingGoalMinutesShouldReturnAuthenticatedUserGoal() {
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        User user = user(1L, "teste", "teste@gmail.com");
        user.setDailyReadingGoalMinutes(30);

        when(tokenService.validateAndDecodeToken("token")).thenReturn(decodedJWT);
        when(decodedJWT.getSubject()).thenReturn("teste@gmail.com");
        when(userRepository.findByEmail("teste@gmail.com")).thenReturn(Optional.of(user));

        assertEquals(30, service.getMyReadingGoalMinutes("Bearer token"));
    }

    @Test
    void updateMyReadingGoalMinutesShouldNormalizeAndSave() {
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        User user = user(1L, "teste", "teste@gmail.com");

        when(tokenService.validateAndDecodeToken("token")).thenReturn(decodedJWT);
        when(decodedJWT.getSubject()).thenReturn("teste@gmail.com");
        when(userRepository.findByEmail("teste@gmail.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.updateMyReadingGoalMinutes("Bearer token", 20);

        assertEquals(20, result.getDailyReadingGoalMinutes());
        verify(userRepository).save(user);
    }

    @Test
    void normalizeReadingGoalShouldUseDefaultForNull() {
        assertEquals(User.DEFAULT_DAILY_READING_GOAL_MINUTES, service.normalizeReadingGoal(null));
    }

    @Test
    void normalizeReadingGoalShouldRejectInvalidValues() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.normalizeReadingGoal(15));
        assertEquals("Meta de leitura inválida. Valores permitidos: 10, 20 ou 30 minutos.", exception.getMessage());
    }

    @Test
    void updateShouldNotCheckDuplicatesWhenValuesDoNotChange() {
        User user = user(1L, "same", "same@gmail.com");
        UserDTO dto = userDto("same", "same@gmail.com", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(1L, dto);

        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).findByEmail("same@gmail.com");
    }

    @Test
    void deleteShouldSkipEmptyPhotoPath() {
        User user = user(1L, "teste", "teste@gmail.com");
        user.setPhotopath("");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        service.delete(1L);

        verify(fileStorageService, never()).deleteFile(anyString());
        verify(userRepository).delete(user);
    }

    @Test
    void createShouldPersistExpectedFields() {
        UserDTO dto = userDto("captured", "captured@gmail.com", "secret");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(roleRepository.findByAuthority("ROLE_USER")).thenReturn(new Role(1L, "ROLE_USER"));
        when(userRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(dto);

        User saved = captor.getValue();
        assertEquals("captured", saved.getUsername());
        assertEquals("captured@gmail.com", saved.getEmail());
        assertEquals("encoded-secret", saved.getPassword());
    }

    private UserDTO userDto(String username, String email, String password) {
        UserDTO dto = new UserDTO();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    private User user(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password");
        return user;
    }
}
