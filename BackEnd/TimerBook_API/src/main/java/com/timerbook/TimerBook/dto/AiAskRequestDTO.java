package com.timerbook.TimerBook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class AiAskRequestDTO {

    @JsonProperty("pdf_path")
    @NotBlank(message = "O caminho do PDF é obrigatório.")
    private String pdfPath;

    @NotBlank(message = "A pergunta é obrigatória.")
    private String question;

    private Integer page;

    @JsonProperty("start_page")
    private Integer startPage;

    @JsonProperty("end_page")
    private Integer endPage;

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getStartPage() {
        return startPage;
    }

    public void setStartPage(Integer startPage) {
        this.startPage = startPage;
    }

    public Integer getEndPage() {
        return endPage;
    }

    public void setEndPage(Integer endPage) {
        this.endPage = endPage;
    }
}
