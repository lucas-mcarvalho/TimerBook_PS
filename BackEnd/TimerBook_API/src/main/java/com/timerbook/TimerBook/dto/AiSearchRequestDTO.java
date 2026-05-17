package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Busca textual em um livro PDF cadastrado")
public class AiSearchRequestDTO {

    @Schema(description = "ID do livro cadastrado no TimerBook", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long bookId;

    @Schema(description = "Termo ou frase que sera buscado no PDF", example = "roteamento", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String query;

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
