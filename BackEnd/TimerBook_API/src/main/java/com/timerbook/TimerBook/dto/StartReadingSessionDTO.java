package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

public class StartReadingSessionDTO {
    @Schema(description = "ID da leitura associada à sessão", example = "1")
    @NotNull
    private Long readingId;
    @Schema(description = "Página inicial da sessão", example = "0")
    @PositiveOrZero
    private Integer startPage;
    @Schema(description = "Data/hora de início da sessão")
    private LocalDateTime startedAt;

    public StartReadingSessionDTO() {}

    public StartReadingSessionDTO(Long readingId, Integer startPage, LocalDateTime startedAt) {
        this.readingId = readingId;
        this.startPage = startPage;
        this.startedAt = startedAt;
    }

    // Getters e Setters
    public Long getReadingId() {
        return readingId;
    }

    public void setReadingId(Long readingId) {
        this.readingId = readingId;
    }

    public Integer getStartPage() {
        return startPage;
    }

    public void setStartPage(Integer startPage) {
        this.startPage = startPage;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
}