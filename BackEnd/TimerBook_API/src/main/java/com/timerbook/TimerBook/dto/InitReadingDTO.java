package com.timerbook.TimerBook.dto;

public class InitReadingDTO {
    private Long bookId;

    public InitReadingDTO() {}

    public InitReadingDTO(Long bookId) {
        this.bookId = bookId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
}