package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Pergunta enviada ao assistente de leitura para um livro cadastrado")
public class AiAskRequestDTO {

    @Schema(description = "ID do livro cadastrado no TimerBook", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long bookId;

    @Schema(description = "Pergunta que o usuario quer fazer sobre o PDF", example = "Do que trata este documento?", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String question;

    @Schema(description = "Pagina do PDF usada como contexto. A numeracao comeca em 1.", example = "1")
    @Positive
    private Integer page;

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
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
}
