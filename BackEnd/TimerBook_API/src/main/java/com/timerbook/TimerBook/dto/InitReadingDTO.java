package com.timerbook.TimerBook.dto;

public class InitReadingDTO {
    private Long bookId;
    private Integer startPage;  // Página inicial da sessão

    public InitReadingDTO() {}

    public InitReadingDTO(Long bookId, Integer startPage) {
        this.bookId = bookId;
        this.startPage = startPage;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Integer getStartPage() {
        return startPage;
    }

    public void setStartPage(Integer startPage) {
        this.startPage = startPage;
    }
}