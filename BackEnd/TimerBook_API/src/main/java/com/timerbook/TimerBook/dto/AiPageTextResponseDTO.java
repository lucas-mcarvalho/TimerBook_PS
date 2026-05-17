package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Texto extraido de uma pagina do PDF")
public class AiPageTextResponseDTO {

    @Schema(description = "Texto puro da pagina solicitada", example = "Capitulo 1...")
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
