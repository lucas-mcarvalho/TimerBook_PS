package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.UserDTO;

import com.timerbook.TimerBook.models.Role;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.RoleRepository;
import com.timerbook.TimerBook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UserService {

    private static final Set<Integer> ALLOWED_READING_GOALS = Set.of(5, 10, 15, 30, 60);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TokenService tokenService;


    @Transactional
    public User create(UserDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Este username já está em uso");
        }

        String photoPath = null;
        if (dto.getPhotopath() != null && !dto.getPhotopath().isEmpty()) {
            photoPath = fileStorageService.storeFile(dto.getPhotopath(), "users");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhotopath(photoPath);
        user.setDailyReadingGoalMinutes(normalizeReadingGoal(dto.getDailyReadingGoalMinutes()));

        Role userRole = roleRepository.findByAuthority("ROLE_USER");
        if (userRole == null) {
            throw new RuntimeException("ROLE_USER não encontrada. Verifique se as migrations foram executadas.");
        }
        user.getRoles().add(userRole);

        return userRepository.save(user);
    }

    public User update(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!user.getUsername().equals(dto.getUsername())) {
            if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
                throw new IllegalArgumentException("Este username já está em uso por outro usuário");
            }
        }
        if (!user.getEmail().equals(dto.getEmail())) {
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Este email já está em uso por outro usuário");
            }
        }
        if (dto.getPhotopath() != null && !dto.getPhotopath().isEmpty()) {
            if (user.getPhotopath() != null) {
                fileStorageService.deleteFile(user.getPhotopath());
            }
            user.setPhotopath(fileStorageService.storeFile(dto.getPhotopath(), "users"));
        }
        else if (Boolean.TRUE.equals(dto.getRemovePhoto())) {
            if (user.getPhotopath() != null) {
                fileStorageService.deleteFile(user.getPhotopath());
            }
            user.setPhotopath(null);
        }
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        return userRepository.save(user);
    }

    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com id: " + id));

        if (user.getPhotopath() != null && !user.getPhotopath().isEmpty()) {
            fileStorageService.deleteFile(user.getPhotopath());
        }
        user.getRoles().clear();
        userRepository.delete(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com id: " + id));
    }


    public User getMe(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token inválido ou ausente");
        }
        String token = authHeader.replace("Bearer ", "");
        var decoded = tokenService.validateAndDecodeToken(token);
        if (decoded == null) {
            throw new RuntimeException("Token inválido");
        }
        String email = decoded.getSubject();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public Integer getMyReadingGoalMinutes(String authHeader) {
        return getMe(authHeader).getDailyReadingGoalMinutes();
    }

    public User updateMyReadingGoalMinutes(String authHeader, Integer dailyReadingGoalMinutes) {
        User user = getMe(authHeader);
        user.setDailyReadingGoalMinutes(normalizeReadingGoal(dailyReadingGoalMinutes));
        return userRepository.save(user);
    }

    public User updateMyCustomReadingGoalMinutes(String authHeader, Integer dailyReadingGoalMinutes) {
        User user = getMe(authHeader);
        
        // Validate if user is PAID
        if (!user.isPaidUser()) {
            throw new IllegalArgumentException("Apenas usuários com plano pago podem definir metas personalizadas. Utilize um dos valores pré-definidos: 5, 10, 15, 30 ou 60 minutos.");
        }
        
        if (dailyReadingGoalMinutes == null || dailyReadingGoalMinutes <= 0) {
            throw new IllegalArgumentException("Meta de leitura inválida. Informe um valor maior que zero.");
        }

        user.setDailyReadingGoalMinutes(dailyReadingGoalMinutes);
        return userRepository.save(user);
    }

    public Integer normalizeReadingGoal(Integer goalMinutes) {
        if (goalMinutes == null) {
            return User.DEFAULT_DAILY_READING_GOAL_MINUTES;
        }
        if (!ALLOWED_READING_GOALS.contains(goalMinutes)) {
            throw new IllegalArgumentException("Meta de leitura inválida. Valores permitidos: 5, 10, 15, 30 ou 60 minutos.");
        }
        return goalMinutes;
    }
}