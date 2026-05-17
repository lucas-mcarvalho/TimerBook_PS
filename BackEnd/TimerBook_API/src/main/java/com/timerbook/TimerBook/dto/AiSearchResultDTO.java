package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ocorrencia encontrada em uma pagina do PDF")
public class AiSearchResultDTO {

    @Schema(description = "Pagina onde o termo aparece", example = "3")
    private Integer page;

    @Schema(description = "Trecho de texto ao redor do termo encontrado", example = "...o roteamento IP define o caminho que os pacotes seguem...")
    private String excerpt;

    public AiSearchResultDTO() {
    }

    public AiSearchResultDTO(Integer page, String excerpt) {
        this.page = page;
        this.excerpt = excerpt;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }
}
