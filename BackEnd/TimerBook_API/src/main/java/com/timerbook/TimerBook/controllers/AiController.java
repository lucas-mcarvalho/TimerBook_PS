package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.controllers.docs.AiControllerDocs;
import com.timerbook.TimerBook.dto.AiAskRequestDTO;
import com.timerbook.TimerBook.dto.AiAskResponseDTO;
import com.timerbook.TimerBook.dto.AiPageTextResponseDTO;
import com.timerbook.TimerBook.dto.AiSearchRequestDTO;
import com.timerbook.TimerBook.dto.AiSearchResponseDTO;
import com.timerbook.TimerBook.services.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/ai")
public class AiController implements AiControllerDocs {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/ask")
    public ResponseEntity<AiAskResponseDTO> ask(@RequestBody AiAskRequestDTO request) {
        return ResponseEntity.ok(aiService.ask(request));
    }

    @PostMapping("/search")
    public ResponseEntity<AiSearchResponseDTO> search(@RequestBody AiSearchRequestDTO request) {
        return ResponseEntity.ok(aiService.search(request));
    }

    @GetMapping("/page-text")
    public ResponseEntity<AiPageTextResponseDTO> pageText(
            @RequestParam Long bookId,
            @RequestParam Integer page
    ) {
        return ResponseEntity.ok(aiService.pageText(bookId, page));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(Map.of(
                "message", exception.getReason() == null ? "Erro ao processar solicitação de IA." : exception.getReason()
        ));
    }
}
