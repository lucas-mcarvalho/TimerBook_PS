package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.dto.AiAskRequestDTO;
import com.timerbook.TimerBook.dto.AiAskResponseDTO;
import com.timerbook.TimerBook.dto.AiPageTextResponseDTO;
import com.timerbook.TimerBook.dto.AiSearchRequestDTO;
import com.timerbook.TimerBook.dto.AiSearchResponseDTO;
import com.timerbook.TimerBook.dto.AiTranslateRequestDTO;
import com.timerbook.TimerBook.dto.AiTranslateResponseDTO;
import com.timerbook.TimerBook.services.AiChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "IA", description = "Endpoints de IA para leitura de PDFs")
public class AiController {

    private final AiChatService aiChatService;

    public AiController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/ask")
    public ResponseEntity<AiAskResponseDTO> ask(@Valid @RequestBody AiAskRequestDTO request) {
        return ResponseEntity.ok(new AiAskResponseDTO(aiChatService.ask(request)));
    }

    @PostMapping("/search")
    public ResponseEntity<AiSearchResponseDTO> search(@Valid @RequestBody AiSearchRequestDTO request) {
        return ResponseEntity.ok(new AiSearchResponseDTO(aiChatService.search(request.getPdfPath(), request.getQuery())));
    }

    @PostMapping("/translate")
    public ResponseEntity<AiTranslateResponseDTO> translate(@Valid @RequestBody AiTranslateRequestDTO request) {
        return ResponseEntity.ok(new AiTranslateResponseDTO(aiChatService.translatePage(request)));
    }

    @GetMapping("/page-text")
    public ResponseEntity<AiPageTextResponseDTO> pageText(
            @RequestParam("pdf_path") String pdfPath,
            @RequestParam int page
    ) {
        return ResponseEntity.ok(new AiPageTextResponseDTO(aiChatService.pageText(pdfPath, page)));
    }
}
