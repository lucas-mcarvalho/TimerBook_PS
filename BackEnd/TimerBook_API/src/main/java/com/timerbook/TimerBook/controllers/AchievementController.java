package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.dto.AchievementDTO;
import com.timerbook.TimerBook.services.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/achievements")
@Tag(name = "Achievements", description = "API para gerenciar conquistas e medalhas")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obter conquistas de um usuário")
    public ResponseEntity<List<AchievementDTO>> getUserMedals(@PathVariable Long userId) {
        List<AchievementDTO> medals = achievementService.getUserMedals(userId);
        return ResponseEntity.ok(medals);
    }
}