package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.AiSearchResultDTO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AiPdfService {

    private final FileStorageService fileStorageService;

    public AiPdfService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public String extractPageText(String pdfPath, int pageNumber) {
        try (PDDocument document = loadPdf(pdfPath)) {
            validatePage(pageNumber, document.getNumberOfPages());
            return extractPageText(document, pageNumber);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Não foi possível ler o PDF.", exception);
        }
    }

    public String extractPageRange(String pdfPath, int start, int end) {
        try (PDDocument document = loadPdf(pdfPath)) {
            int totalPages = document.getNumberOfPages();
            int safeStart = Math.max(1, start);
            int safeEnd = Math.min(end, totalPages);

            if (safeStart > safeEnd || safeStart > totalPages) {
                return "";
            }

            List<String> chunks = new ArrayList<>();
            for (int page = safeStart; page <= safeEnd; page++) {
                chunks.add("--- Página " + page + " ---\n" + extractPageText(document, page));
            }

            return String.join("\n\n", chunks);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Não foi possível ler o PDF.", exception);
        }
    }

    public int getPageCount(String pdfPath) {
        try (PDDocument document = loadPdf(pdfPath)) {
            return document.getNumberOfPages();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Não foi possível ler o PDF.", exception);
        }
    }

    public List<AiSearchResultDTO> search(String pdfPath, String query) {
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        List<AiSearchResultDTO> results = new ArrayList<>();

        try (PDDocument document = loadPdf(pdfPath)) {
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                String text = extractPageText(document, page);
                String normalizedText = text.toLowerCase(Locale.ROOT);
                int matchIndex = normalizedText.indexOf(normalizedQuery);

                if (matchIndex >= 0) {
                    int start = Math.max(0, matchIndex - 45);
                    int end = Math.min(text.length(), matchIndex + query.length() + 75);
                    results.add(new AiSearchResultDTO(page, text.substring(start, end).trim()));
                }
            }

            return results;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Não foi possível ler o PDF.", exception);
        }
    }

    private PDDocument loadPdf(String pdfPath) throws IOException {
        if (pdfPath == null || pdfPath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O caminho do PDF é obrigatório.");
        }

        try {
            return Loader.loadPDF(fileStorageService.loadFile(pdfPath));
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PDF não encontrado: " + pdfPath);
        }
    }

    private String extractPageText(PDDocument document, int pageNumber) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        textStripper.setStartPage(pageNumber);
        textStripper.setEndPage(pageNumber);
        return textStripper.getText(document);
    }

    private void validatePage(int pageNumber, int totalPages) {
        if (pageNumber < 1 || pageNumber > totalPages) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Página " + pageNumber + " não existe neste PDF.");
        }
    }
}
