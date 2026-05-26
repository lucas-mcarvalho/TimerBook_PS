package com.timerbook.TimerBook.dto.billing;

public class CheckoutSessionResponse {
    private String sessionId;
    private String checkoutUrl;

    public CheckoutSessionResponse() {}

    public CheckoutSessionResponse(String sessionId, String checkoutUrl) {
        this.sessionId = sessionId;
        this.checkoutUrl = checkoutUrl;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }
}
