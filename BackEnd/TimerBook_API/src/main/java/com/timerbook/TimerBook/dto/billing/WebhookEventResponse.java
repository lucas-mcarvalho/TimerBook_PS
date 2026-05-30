package com.timerbook.TimerBook.dto.billing;

import java.time.LocalDateTime;

public class WebhookEventResponse {
    private Long id;
    private String provider;
    private String providerEventId;
    private String eventType;
    private String processingStatus;
    private String payloadHash;
    private String rawPayload;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;

    public WebhookEventResponse() {}

    public WebhookEventResponse(Long id, String provider, String providerEventId, String eventType, String processingStatus, String payloadHash, String rawPayload, LocalDateTime processedAt, LocalDateTime createdAt) {
        this.id = id;
        this.provider = provider;
        this.providerEventId = providerEventId;
        this.eventType = eventType;
        this.processingStatus = processingStatus;
        this.payloadHash = payloadHash;
        this.rawPayload = rawPayload;
        this.processedAt = processedAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderEventId() {
        return providerEventId;
    }

    public void setProviderEventId(String providerEventId) {
        this.providerEventId = providerEventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    public String getPayloadHash() {
        return payloadHash;
    }

    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}