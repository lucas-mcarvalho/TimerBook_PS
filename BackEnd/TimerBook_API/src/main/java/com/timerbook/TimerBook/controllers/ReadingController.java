package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.controllers.docs.ReadingControllerDocs;
import com.timerbook.TimerBook.dto.FinishReadingDTO;
import com.timerbook.TimerBook.dto.InitReadingDTO;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.services.ReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @PostMapping("/start")
    public ResponseEntity<Reading> startReading(
            @RequestBody InitReadingDTO dto) {
        try {
            Reading reading = readingService.initializeReading(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(reading);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{readingId}/finish")

    public ResponseEntity<Reading> finishReading(
            @PathVariable Long readingId,
            @RequestBody FinishReadingDTO dto) {
        try {
            Reading reading = readingService.finishReading(readingId, dto);
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


    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Reading>> getReadingsByBookId(
            @PathVariable Long bookId) {
        try {
            List<Reading> readings = readingService.getReadingsByBookId(bookId);
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