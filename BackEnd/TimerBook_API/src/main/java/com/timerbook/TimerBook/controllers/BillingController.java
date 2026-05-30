package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.controllers.docs.BillingControllerDocs;
import com.timerbook.TimerBook.dto.billing.CheckoutSessionRequest;
import com.timerbook.TimerBook.dto.billing.CheckoutSessionResponse;
import com.timerbook.TimerBook.dto.billing.CustomerPortalResponse;
import com.timerbook.TimerBook.dto.billing.SubscriptionResponse;
import com.timerbook.TimerBook.dto.billing.WebhookEventResponse;
import com.timerbook.TimerBook.services.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/billing")
public class BillingController implements BillingControllerDocs {

    @Autowired
    private BillingService billingService;

    @PostMapping("/checkout-session")
    @Override
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CheckoutSessionRequest request
    ) {
        CheckoutSessionResponse resp = billingService.createCheckoutSession(authHeader, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping("/webhook/{provider}")
    @Override
    public ResponseEntity<String> handleWebhook(
            @PathVariable String provider,
            @RequestBody String payload,
            HttpServletRequest request,
            @RequestHeader Map<String, String> headers
    ) {
        Map<String, String> webhookHeaders = new java.util.HashMap<>(headers);
        webhookHeaders.put("__query_string__", request.getQueryString() == null ? "" : request.getQueryString());
        webhookHeaders.put("__request_uri__", request.getRequestURI() == null ? "" : request.getRequestURI());

        String signature = getHeaderIgnoreCase(headers, "x-signature");
        boolean processed = billingService.handleWebhook(provider.toUpperCase(), payload, signature, webhookHeaders);
        if (processed) return ResponseEntity.ok("received");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ignored");
    }

    @GetMapping("/webhooks")
    @Override
    public ResponseEntity<List<WebhookEventResponse>> listWebhookEvents(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer limit
    ) {
        billingService.getMySubscription(authHeader);
        return ResponseEntity.ok(billingService.listWebhookEvents(limit));
    }

    @GetMapping("/subscription/me")
    @Override
    public ResponseEntity<SubscriptionResponse> getMySubscription(
            @RequestHeader("Authorization") String authHeader
    ) {
        SubscriptionResponse resp = billingService.getMySubscription(authHeader);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/customer-portal")
    @Override
    public ResponseEntity<CustomerPortalResponse> createCustomerPortal(
            @RequestHeader("Authorization") String authHeader
    ) {
        String url = billingService.createCustomerPortal(authHeader);
        return ResponseEntity.ok(new CustomerPortalResponse(url));
    }

    private String getHeaderIgnoreCase(Map<String, String> headers, String name) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return "";
    }
}
