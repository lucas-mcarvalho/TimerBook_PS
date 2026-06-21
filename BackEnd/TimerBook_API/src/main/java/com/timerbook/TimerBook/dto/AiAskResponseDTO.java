package com.timerbook.TimerBook.dto;

public class AiAskResponseDTO {

    private String answer;

    public AiAskResponseDTO() {
    }

    public AiAskResponseDTO(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
