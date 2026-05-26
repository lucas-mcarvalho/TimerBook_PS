package com.timerbook.TimerBook.dto.billing;

public class CustomerPortalResponse {
    private String url;

    public CustomerPortalResponse() {}

    public CustomerPortalResponse(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
