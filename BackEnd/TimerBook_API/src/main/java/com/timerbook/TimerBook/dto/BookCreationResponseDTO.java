package com.timerbook.TimerBook.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.timerbook.TimerBook.models.Book;

import java.util.List;

public class BookCreationResponseDTO {

    @JsonUnwrapped
    private Book book;
    private List<AchievementDTO> novasConquistas;

    public BookCreationResponseDTO() {
    }

    public BookCreationResponseDTO(Book book, List<AchievementDTO> novasConquistas) {
        this.book = book;
        this.novasConquistas = novasConquistas;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public List<AchievementDTO> getNovasConquistas() {
        return novasConquistas;
    }

    public void setNovasConquistas(List<AchievementDTO> novasConquistas) {
        this.novasConquistas = novasConquistas;
    }
}
