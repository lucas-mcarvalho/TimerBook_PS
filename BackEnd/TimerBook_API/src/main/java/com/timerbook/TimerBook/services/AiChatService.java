package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.AiAskRequestDTO;
import com.timerbook.TimerBook.dto.AiSearchResultDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AiChatService {

    private static final int PAGE_RANGE = 2;

    private final AiPdfService aiPdfService;
    private final AiOllamaService aiOllamaService;

    public AiChatService(AiPdfService aiPdfService, AiOllamaService aiOllamaService) {
        this.aiPdfService = aiPdfService;
        this.aiOllamaService = aiOllamaService;
    }

    public String ask(AiAskRequestDTO request) {
        String context;

        if (request.getPage() != null) {
            int start = Math.max(1, request.getPage() - PAGE_RANGE);
            int end = request.getPage() + PAGE_RANGE;
            context = aiPdfService.extractPageRange(request.getPdfPath(), start, end);
        } else if (request.getStartPage() != null && request.getEndPage() != null) {
            context = aiPdfService.extractPageRange(request.getPdfPath(), request.getStartPage(), request.getEndPage());
        } else {
            context = aiPdfService.extractPageRange(request.getPdfPath(), 1, 10);
        }

        if (context.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Nenhum texto encontrado nas páginas solicitadas.");
        }

        return aiOllamaService.askModel(context, request.getQuestion());
    }

    public List<AiSearchResultDTO> search(String pdfPath, String query) {
        return aiPdfService.search(pdfPath, query);
    }

    public String pageText(String pdfPath, int page) {
        return aiPdfService.extractPageText(pdfPath, page);
    }
}
