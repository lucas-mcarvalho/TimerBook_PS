package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultado da busca textual no PDF")
public class AiSearchResponseDTO {

    @Schema(description = "Paginas em que o termo foi encontrado")
    private List<AiSearchResultDTO> results;

    public AiSearchResponseDTO() {
    }

    public AiSearchResponseDTO(List<AiSearchResultDTO> results) {
        this.results = results;
    }

    public List<AiSearchResultDTO> getResults() {
        return results;
    }

    public void setResults(List<AiSearchResultDTO> results) {
        this.results = results;
    }
}
