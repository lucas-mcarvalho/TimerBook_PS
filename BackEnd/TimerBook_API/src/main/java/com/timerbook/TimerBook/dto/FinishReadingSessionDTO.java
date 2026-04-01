package com.timerbook.TimerBook.dto;

public class FinishReadingSessionDTO {
    private Integer endPage;

    public FinishReadingSessionDTO() {}

    public FinishReadingSessionDTO(Integer endPage) {
        this.endPage = endPage;
    }

    public Integer getEndPage() {
        return endPage;
    }

    public void setEndPage(Integer endPage) {
        this.endPage = endPage;
    }
}
