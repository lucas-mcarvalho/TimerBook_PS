package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Objeto de transferência de dados para iniciar uma nova leitura")
public class InitReadingDTO {

    @Schema(
            description = "ID numérico do livro que o usuário deseja começar a ler",
            example = "12",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
        @NotNull
    private Long bookId;
    @Schema(
            description = "Página onde a leitura está sendo iniciada. Útil caso o usuário já tenha lido parte do livro antes de usar o app. Se não for enviado, o padrão é 0.",
            example = "0",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            defaultValue = "0"
    )
            @PositiveOrZero
            private Integer startPage;  // Página inicial da sessão

    public InitReadingDTO() {}

    public InitReadingDTO(Long bookId, Integer startPage) {
        this.bookId = bookId;
        this.startPage = startPage;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Integer getStartPage() {
        return startPage;
    }

    public void setStartPage(Integer startPage) {
        this.startPage = startPage;
    }
}