package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.services.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@RestController
public class FileController {

    private static final String UPLOADS_PREFIX = "/uploads/";

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping({"/uploads/**", "/api/uploads/**"})
    public ResponseEntity<byte[]> getFile(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        int prefixIndex = requestUri.indexOf(UPLOADS_PREFIX);

        if (prefixIndex < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Caminho de arquivo inválido.");
        }

        String filePath = UriUtils.decode(requestUri.substring(prefixIndex + 1), StandardCharsets.UTF_8);

        try {
            byte[] file = fileStorageService.loadFile(filePath);
            String contentType = fileStorageService.getContentType(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                    .body(file);
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo não encontrado.", exception);
        }
    }
}
