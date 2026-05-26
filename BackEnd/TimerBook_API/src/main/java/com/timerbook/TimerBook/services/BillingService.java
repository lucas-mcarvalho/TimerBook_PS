package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.billing.CheckoutSessionRequest;
import com.timerbook.TimerBook.dto.billing.CheckoutSessionResponse;
import com.timerbook.TimerBook.dto.billing.SubscriptionResponse;

import java.util.Map;

public interface BillingService {
    CheckoutSessionResponse createCheckoutSession(String authHeader, CheckoutSessionRequest request);

    boolean handleWebhook(String provider, String payload, String signature, Map<String,String> headers);

    SubscriptionResponse getMySubscription(String authHeader);

    String createCustomerPortal(String authHeader);
}
