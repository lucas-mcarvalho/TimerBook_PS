package com.timerbook.TimerBook.dto;

public class FinishReadingDTO {
    private Integer finalPage;
    private String notes;

    public FinishReadingDTO() {}

    public FinishReadingDTO(Integer finalPage, String notes) {
        this.finalPage = finalPage;
        this.notes = notes;
    }

    public Integer getFinalPage() {
        return finalPage;
    }

    public void setFinalPage(Integer finalPage) {
        this.finalPage = finalPage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}