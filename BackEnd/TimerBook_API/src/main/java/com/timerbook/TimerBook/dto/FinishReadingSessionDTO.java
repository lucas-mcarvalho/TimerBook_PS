package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class FinishReadingSessionDTO {
    @Schema(description = "Página final da sessão", example = "120")
    @NotNull
    @PositiveOrZero
    private Integer endPage;

    public FinishReadingSessionDTO() {}

    public FinishReadingSessionDTO(Integer endPage) {
        this.endPage = endPage;
    }

    public Integer getEndPage() {
        return endPage;
    }

    public void setEndPage(Integer endPage) {
        this.endPage = endPage;
    }
}
