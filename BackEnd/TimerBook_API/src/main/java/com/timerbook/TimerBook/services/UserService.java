package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.UserDTO;

import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private PasswordEncoder passwordEncoder;



    public User create(UserDTO dto) {
        String photoPath = null;
        if (dto.getPhotopath() != null && !dto.getPhotopath().isEmpty()) {
            photoPath = fileStorageService.storeFile(dto.getPhotopath(), "users");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhotopath(photoPath);

        return userRepository.save(user);
    }

    public User update(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));


        if (dto.getPhotopath() != null && !dto.getPhotopath().isEmpty()) {
            fileStorageService.deleteFile(user.getPhotopath()); // deleta a antiga
            String newPhotoPath = fileStorageService.storeFile(dto.getPhotopath(), "users");
            user.setPhotopath(newPhotoPath);
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
}