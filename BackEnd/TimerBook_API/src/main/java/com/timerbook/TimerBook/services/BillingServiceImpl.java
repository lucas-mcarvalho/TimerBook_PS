package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.billing.CheckoutSessionRequest;
import com.timerbook.TimerBook.dto.billing.CheckoutSessionResponse;
import com.timerbook.TimerBook.dto.billing.SubscriptionResponse;
import com.timerbook.TimerBook.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class BillingServiceImpl implements BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingServiceImpl.class);

    @Autowired
    private UserService userService;

    @Value("${payment.provider:STRIPE}")
    private String defaultProvider;

    @Override
    public CheckoutSessionResponse createCheckoutSession(String authHeader, CheckoutSessionRequest request) {
        User user = userService.getMe(authHeader);

        // Placeholder: here you would call the provider SDK to create a real session
        String sessionId = "sess_" + System.currentTimeMillis();
        String checkoutUrl = "https://sandbox.example-payments/checkout/" + sessionId;

        logger.info("Created dummy checkout session for user {} sessionId={}", user.getEmail(), sessionId);

        return new CheckoutSessionResponse(sessionId, checkoutUrl);
    }

    @Override
    public boolean handleWebhook(String provider, String payload, String signature, Map<String, String> headers) {
        // Minimal stub: log and mark processed. Real impl must validate signature and persist events.
        logger.info("Received webhook from provider {} signature={} payloadSize={}", provider, signature, payload == null ? 0 : payload.length());
        return true;
    }

    @Override
    public SubscriptionResponse getMySubscription(String authHeader) {
        User user = userService.getMe(authHeader);
        String plan = user.getSubscriptionPlan();
        String status = "PENDING";
        if ("PAID".equals(plan)) status = "ACTIVE"; else status = "FREE";

        return new SubscriptionResponse(defaultProvider, status, null, null);
    }

    @Override
    public String createCustomerPortal(String authHeader) {
        User user = userService.getMe(authHeader);
        // Placeholder URL
        String url = "https://sandbox.example-payments/customer-portal/" + user.getId();
        return url;
    }
}
