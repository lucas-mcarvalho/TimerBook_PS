package com.timerbook.TimerBook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AiTranslateRequestDTO {

    @JsonProperty("pdf_path")
    @NotBlank(message = "O caminho do PDF é obrigatório.")
    private String pdfPath;

    @NotNull(message = "A página é obrigatória.")
    @Positive(message = "A página deve ser maior que zero.")
    private Integer page;

    @JsonProperty("target_language")
    private String targetLanguage;

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
}
