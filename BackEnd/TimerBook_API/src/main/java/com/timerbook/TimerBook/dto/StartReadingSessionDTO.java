package com.timerbook.TimerBook.dto;

import java.time.LocalDateTime;

public class StartReadingSessionDTO {
    private Long readingId;
    private Integer startPage;
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