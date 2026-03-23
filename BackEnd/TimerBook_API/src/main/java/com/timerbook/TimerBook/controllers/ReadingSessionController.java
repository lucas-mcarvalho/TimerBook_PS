package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.dto.StartReadingSessionDTO;
import com.timerbook.TimerBook.models.ReadingSession;
import com.timerbook.TimerBook.services.ReadingSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reading-sessions")
public class ReadingSessionController {

    @Autowired
    private ReadingSessionService readingSessionService;

    @PostMapping("/start")
    public ResponseEntity<ReadingSession> startReading(@RequestBody StartReadingSessionDTO dto) {
        try {
            ReadingSession session = readingSessionService.startReadingSession(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(session);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}