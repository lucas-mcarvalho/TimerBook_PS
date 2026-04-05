package com.timerbook.TimerBook.dto;

public class ReadingStatsDTO {
    private Long readingId;
    private Integer pagesRead;
    private Long totalSeconds;
    private Double averageSecondsPerSession;
    private Long sessionsCount;
    private Integer currentStreakDays;
    private Integer maxStreakDays;

    public ReadingStatsDTO() {}

    public Long getReadingId() {
        return readingId;
    }

    public void setReadingId(Long readingId) {
        this.readingId = readingId;
    }

    public Integer getPagesRead() {
        return pagesRead;
    }

    public void setPagesRead(Integer pagesRead) {
        this.pagesRead = pagesRead;
    }

    public Long getTotalSeconds() {
        return totalSeconds;
    }

    public void setTotalSeconds(Long totalSeconds) {
        this.totalSeconds = totalSeconds;
    }

    public Double getAverageSecondsPerSession() {
        return averageSecondsPerSession;
    }

    public void setAverageSecondsPerSession(Double averageSecondsPerSession) {
        this.averageSecondsPerSession = averageSecondsPerSession;
    }

    public Long getSessionsCount() {
        return sessionsCount;
    }

    public void setSessionsCount(Long sessionsCount) {
        this.sessionsCount = sessionsCount;
    }

    public Integer getCurrentStreakDays() {
        return currentStreakDays;
    }

    public void setCurrentStreakDays(Integer currentStreakDays) {
        this.currentStreakDays = currentStreakDays;
    }

    public Integer getMaxStreakDays() {
        return maxStreakDays;
    }

    public void setMaxStreakDays(Integer maxStreakDays) {
        this.maxStreakDays = maxStreakDays;
    }
}
