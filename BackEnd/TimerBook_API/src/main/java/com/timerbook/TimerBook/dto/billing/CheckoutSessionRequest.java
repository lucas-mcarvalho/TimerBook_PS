package com.timerbook.TimerBook.dto.billing;

public class CheckoutSessionRequest {
    private String planId;
    private String successUrl;
    private String cancelUrl;

    public CheckoutSessionRequest() {}

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }
}
