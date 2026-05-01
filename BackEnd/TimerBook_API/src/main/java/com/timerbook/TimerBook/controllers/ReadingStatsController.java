package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.controllers.docs.ReadingStatsControllerDocs;
import com.timerbook.TimerBook.dto.ReadingStatsDTO;
import com.timerbook.TimerBook.dto.UserReadingGoalStreakDTO;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.repository.UserRepository;
import com.timerbook.TimerBook.services.ReadingStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/stats")
public class ReadingStatsController implements ReadingStatsControllerDocs {

    @Autowired
    private ReadingStatsService service;

    @Autowired
    private UserRepository userRepository;

    private Long getLoggedUserId() {
        String identificador = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(identificador)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado no banco de dados"))
                .getId();
    }

    @GetMapping("/books-in-progress")
    public ResponseEntity<List<Reading>> getBooksInProgress() {
        Long userId = getLoggedUserId();
        List<Reading> readings = service.getReadingsInProgress(userId);
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/reading/{readingId}")
    public ResponseEntity<ReadingStatsDTO> getReadingStats(
            @PathVariable Long readingId,
            @RequestParam(value = "start", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(value = "end", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(value = "includeOngoing", required = false, defaultValue = "false") boolean includeOngoing
    ) {
        try {
            Long userId = getLoggedUserId();
            ReadingStatsDTO dto = service.getStatsForReading(userId, readingId, start, end, includeOngoing);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).build();
        }
    }

    @GetMapping("/user/general")
    public ResponseEntity<ReadingStatsDTO> getUserGeneralStats(
            @RequestParam(value = "start", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(value = "end", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(value = "includeOngoing", required = false, defaultValue = "false") boolean includeOngoing
    ) {
        Long userId = getLoggedUserId();
        ReadingStatsDTO dto = service.getGeneralStatsForUser(userId, start, end, includeOngoing);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/reading/{readingId}/streak")
    public ResponseEntity<Integer> getReadingStreak(
            @PathVariable Long readingId,
            @RequestParam(value = "start", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(value = "end", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        try {
            Long userId = getLoggedUserId();
            ReadingStatsDTO dto = service.getStatsForReading(userId, readingId, start, end, false);
            return ResponseEntity.ok(dto.getCurrentStreakDays());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).build();
        }
    }

    @GetMapping("/user/streak")
    public ResponseEntity<UserReadingGoalStreakDTO> getUserGoalStreak(
            @RequestParam(value = "start", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(value = "end", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        Long userId = getLoggedUserId();
        UserReadingGoalStreakDTO dto = service.getUserGoalStreak(userId, start, end);
        return ResponseEntity.ok(dto);
    }
}