package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.controllers.docs.ReadingControllerDocs;
import com.timerbook.TimerBook.dto.FinishReadingDTO;
import com.timerbook.TimerBook.dto.InitReadingDTO;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.services.ReadingService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/readings")
public class ReadingController implements ReadingControllerDocs {

    @Autowired
    private ReadingService readingService;

    @PostMapping("/{userId}/start")
    public ResponseEntity<Reading> startReading(
            @PathVariable Long userId,
            @Valid @RequestBody InitReadingDTO dto) {
        try {
            Reading reading = readingService.initializeReading(userId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(reading);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{userId}/{readingId}/finish")
    public ResponseEntity<Reading> finishReading(
            @PathVariable Long userId,
            @PathVariable Long readingId,
            @Valid @RequestBody FinishReadingDTO dto) {
        try {
            Reading reading = readingService.finishReading(userId, readingId, dto);
            return ResponseEntity.ok(reading);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{readingId}")
    public ResponseEntity<Reading> getReading(
            @Parameter(
                    name = "readingId",
                    description = "ID da leitura a ser obtida",
                    required = true,
                    example = "1"
            )
            @PathVariable Long readingId) {
        try {
            Reading reading = readingService.getReadingById(readingId);
            return ResponseEntity.ok(reading);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{userId}/{bookId}")
    public ResponseEntity<List<Reading>> getReadingsByBookId(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        try {
            List<Reading> readings = readingService.getReadingsByBookId(bookId, userId);
            return ResponseEntity.ok(readings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Reading>> getAllReadings() {
        List<Reading> readings = readingService.getAll();
        return ResponseEntity.ok(readings);
    }
}
