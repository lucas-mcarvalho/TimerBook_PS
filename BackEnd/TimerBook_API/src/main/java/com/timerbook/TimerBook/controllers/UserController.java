package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.controllers.docs.UserControllerDocs;
import com.timerbook.TimerBook.dto.UserReadingGoalRequestDTO;
import com.timerbook.TimerBook.dto.UserReadingGoalResponseDTO;
import com.timerbook.TimerBook.dto.UserDTO;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserController implements UserControllerDocs {
    @Autowired
    private  UserService userService;



    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> createFromJson(@RequestBody UserDTO dto) {
        User user = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> create(
            @RequestPart("username") String username,
            @RequestPart("email") String email,
            @RequestPart("password") String password,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {

        UserDTO dto = new UserDTO();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setPhotopath(photo);

        User user = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> update(
            @PathVariable Long id,
            @RequestPart("username") String username,
            @RequestPart("email") String email,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @RequestPart(value = "removePhoto", required = false) String removePhotoStr) {
        UserDTO dto = new UserDTO();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setPhotopath(photo);

        if (removePhotoStr != null) {
            dto.setRemovePhoto(Boolean.parseBoolean(removePhotoStr));
        }

        return ResponseEntity.ok(userService.update(id, dto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMe(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(userService.getMe(authHeader));
    }

    @GetMapping("/me/reading-goal")
    public ResponseEntity<UserReadingGoalResponseDTO> getMyReadingGoal(
            @RequestHeader("Authorization") String authHeader) {
        Integer goal = userService.getMyReadingGoalMinutes(authHeader);
        return ResponseEntity.ok(new UserReadingGoalResponseDTO(goal));
    }

    @PutMapping("/me/reading-goal")
    public ResponseEntity<UserReadingGoalResponseDTO> updateMyReadingGoal(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UserReadingGoalRequestDTO body) {
        try {
            User user = userService.updateMyReadingGoalMinutes(authHeader, body.getDailyReadingGoalMinutes());
            return ResponseEntity.ok(new UserReadingGoalResponseDTO(user.getDailyReadingGoalMinutes()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}