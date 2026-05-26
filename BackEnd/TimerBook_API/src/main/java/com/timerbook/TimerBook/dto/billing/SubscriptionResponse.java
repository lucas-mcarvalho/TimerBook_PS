package com.timerbook.TimerBook.dto.billing;

import java.time.Instant;

public class SubscriptionResponse {
    private String provider;
    private String status;
    private Instant currentPeriodEnd;
    private String providerSubscriptionId;

    public SubscriptionResponse() {}

    public SubscriptionResponse(String provider, String status, Instant currentPeriodEnd, String providerSubscriptionId) {
        this.provider = provider;
        this.status = status;
        this.currentPeriodEnd = currentPeriodEnd;
        this.providerSubscriptionId = providerSubscriptionId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public void setCurrentPeriodEnd(Instant currentPeriodEnd) {
        this.currentPeriodEnd = currentPeriodEnd;
    }

    public String getProviderSubscriptionId() {
        return providerSubscriptionId;
    }

    public void setProviderSubscriptionId(String providerSubscriptionId) {
        this.providerSubscriptionId = providerSubscriptionId;
    }
}
