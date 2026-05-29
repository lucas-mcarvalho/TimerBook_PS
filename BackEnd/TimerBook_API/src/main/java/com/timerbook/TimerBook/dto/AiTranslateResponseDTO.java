package com.timerbook.TimerBook.dto;

public class AiTranslateResponseDTO {

    private String translation;

    public AiTranslateResponseDTO() {
    }

    public AiTranslateResponseDTO(String translation) {
        this.translation = translation;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }
}
