package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.controllers.docs.ReadingStatsControllerDocs;
import com.timerbook.TimerBook.dto.ReadingStatsDTO;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.services.ReadingStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/stats")

public class ReadingStatsController implements ReadingStatsControllerDocs {

    @Autowired
    private ReadingStatsService service;

    @GetMapping("/books-in-progress")
    public ResponseEntity<List<Reading>> getBooksInProgress() {
        List<Reading> readings = service.getReadingsInProgress();
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
        ReadingStatsDTO dto = service.getStatsForReading(readingId, start, end, includeOngoing);
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
        ReadingStatsDTO dto = service.getStatsForReading(readingId, start, end, false);
        return ResponseEntity.ok(dto.getCurrentStreakDays());
    }
}