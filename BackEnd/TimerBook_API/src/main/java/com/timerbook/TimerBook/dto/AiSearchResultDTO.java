package com.timerbook.TimerBook.dto;

public class AiSearchResultDTO {

    private int page;
    private String excerpt;

    public AiSearchResultDTO() {
    }

    public AiSearchResultDTO(int page, String excerpt) {
        this.page = page;
        this.excerpt = excerpt;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }
}
