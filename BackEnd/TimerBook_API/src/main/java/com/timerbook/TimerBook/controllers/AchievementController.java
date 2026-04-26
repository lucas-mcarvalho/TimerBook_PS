package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.dto.AchievementDTO;
import com.timerbook.TimerBook.services.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/achievements")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AchievementDTO>> getUserMedals(@PathVariable Long userId) {
        List<AchievementDTO> medals = achievementService.getUserMedals(userId);
        return ResponseEntity.ok(medals);
    }
}