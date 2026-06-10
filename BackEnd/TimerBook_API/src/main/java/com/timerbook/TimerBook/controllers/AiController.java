package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.dto.AiAskRequestDTO;
import com.timerbook.TimerBook.dto.AiAskResponseDTO;
import com.timerbook.TimerBook.dto.AiPageTextResponseDTO;
import com.timerbook.TimerBook.dto.AiSearchRequestDTO;
import com.timerbook.TimerBook.dto.AiSearchResponseDTO;
import com.timerbook.TimerBook.dto.AiTranslateRequestDTO;
import com.timerbook.TimerBook.dto.AiTranslateResponseDTO;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.services.AiChatService;
import com.timerbook.TimerBook.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "IA", description = "Endpoints de IA para leitura de PDFs")
public class AiController {

    private final AiChatService aiChatService;
    private final UserService userService;

    public AiController(AiChatService aiChatService, UserService userService) {
        this.aiChatService = aiChatService;
        this.userService = userService;
    }

    @PostMapping("/ask")
    public ResponseEntity<AiAskResponseDTO> ask(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AiAskRequestDTO request
    ) {
        assertPremiumUser(authHeader);
        return ResponseEntity.ok(new AiAskResponseDTO(aiChatService.ask(request)));
    }

    @PostMapping("/search")
    public ResponseEntity<AiSearchResponseDTO> search(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AiSearchRequestDTO request
    ) {
        assertPremiumUser(authHeader);
        return ResponseEntity.ok(new AiSearchResponseDTO(aiChatService.search(request.getPdfPath(), request.getQuery())));
    }

    @PostMapping("/translate")
    public ResponseEntity<AiTranslateResponseDTO> translate(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AiTranslateRequestDTO request
    ) {
        assertPremiumUser(authHeader);
        return ResponseEntity.ok(new AiTranslateResponseDTO(aiChatService.translatePage(request)));
    }

    @GetMapping("/page-text")
    public ResponseEntity<AiPageTextResponseDTO> pageText(
            @RequestParam("pdf_path") String pdfPath,
            @RequestParam int page
    ) {
        return ResponseEntity.ok(new AiPageTextResponseDTO(aiChatService.pageText(pdfPath, page)));
    }

    private void assertPremiumUser(String authHeader) {
        User user = userService.getMe(authHeader);
        if (!user.isPaidUser()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Recursos de IA são exclusivos do plano Premium."
            );
        }
    }
}
