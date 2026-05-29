package com.timerbook.TimerBook.dto;

import java.util.List;

public class AiSearchResponseDTO {

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
