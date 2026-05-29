package com.timerbook.TimerBook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class AiSearchRequestDTO {

    @JsonProperty("pdf_path")
    @NotBlank(message = "O caminho do PDF é obrigatório.")
    private String pdfPath;

    @NotBlank(message = "O termo de busca é obrigatório.")
    private String query;

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
