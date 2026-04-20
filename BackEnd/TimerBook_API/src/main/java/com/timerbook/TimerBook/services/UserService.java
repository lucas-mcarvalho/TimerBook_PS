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

@Service
public class UserService {

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

        String photoPath = null;
        if (dto.getPhotopath() != null && !dto.getPhotopath().isEmpty()) {
            photoPath = fileStorageService.storeFile(dto.getPhotopath(), "users");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhotopath(photoPath);

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
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        fileStorageService.deleteFile(user.getPhotopath());

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
}