package com.timerbook.TimerBook.dto;

public class AiPageTextResponseDTO {

    private String text;

    public AiPageTextResponseDTO() {
    }

    public AiPageTextResponseDTO(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
