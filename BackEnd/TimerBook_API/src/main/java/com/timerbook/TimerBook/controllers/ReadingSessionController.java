package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.controllers.docs.ReadingSessionControllerDocs;
import com.timerbook.TimerBook.dto.FinishReadingSessionDTO;
import com.timerbook.TimerBook.dto.FinishSessionResponseDTO;
import com.timerbook.TimerBook.dto.StartReadingSessionDTO;
import com.timerbook.TimerBook.models.ReadingSession;
import com.timerbook.TimerBook.services.ReadingSessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reading-sessions")
public class ReadingSessionController implements ReadingSessionControllerDocs {

    @Autowired
    private ReadingSessionService readingSessionService;

    @PostMapping("/start")
    public ResponseEntity<ReadingSession> startReading(@Valid @RequestBody StartReadingSessionDTO dto) {
        try {
            ReadingSession session = readingSessionService.startReadingSession(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(session);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{sessionId}/finish")
    public ResponseEntity<FinishSessionResponseDTO> finishReadingSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody FinishReadingSessionDTO dto) {
        try {
            FinishSessionResponseDTO response = readingSessionService.finishReadingSession(sessionId, dto.getEndPage());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReadingSession> getSessionById(@PathVariable Long id){
        try {
            ReadingSession session = readingSessionService.getById(id);
            return ResponseEntity.ok(session);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ReadingSession>> getAllSessions() {
        List<ReadingSession> sessions = readingSessionService.getAll();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/reading/{readingId}")
    public ResponseEntity<List<ReadingSession>> getSessionsByReadingId(
            @PathVariable Long readingId) {
        try {
            List<ReadingSession> sessions = readingSessionService.getSessionsByReadingId(readingId);
            return ResponseEntity.ok(sessions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}