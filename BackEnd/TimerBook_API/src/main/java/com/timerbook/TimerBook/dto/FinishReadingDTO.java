package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;

public class FinishReadingDTO {
    @Schema(description = "Página final da leitura", example = "100")
    @PositiveOrZero
    private Integer finalPage;
    @Schema(description = "Anotações da leitura")
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