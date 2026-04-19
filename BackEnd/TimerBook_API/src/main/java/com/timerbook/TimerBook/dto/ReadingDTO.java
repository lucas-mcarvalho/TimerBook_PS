package com.timerbook.TimerBook.dto;

import com.timerbook.TimerBook.models.Reading;
import java.time.LocalDateTime;

public class ReadingDTO {

    private Long id;
    private Long bookId;
    private String bookName;
    private Long userId;
    private Integer currentPage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public ReadingDTO() {
    }


    public ReadingDTO(Reading reading) {
        this.id = reading.getId();
        this.currentPage = reading.getCurrentPage();
        this.startedAt = reading.getStartedAt();
        this.finishedAt = reading.getFinishedAt();


        if (reading.getBook() != null) {
            this.bookId = reading.getBook().getId();
            this.bookName = reading.getBook().getName();
        }


        if (reading.getUser() != null) {
            this.userId = reading.getUser().getId();
        }
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }



}