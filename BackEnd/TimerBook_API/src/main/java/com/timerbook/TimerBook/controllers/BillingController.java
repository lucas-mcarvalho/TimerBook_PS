package com.timerbook.TimerBook.controllers;

import com.timerbook.TimerBook.dto.billing.CheckoutSessionRequest;
import com.timerbook.TimerBook.dto.billing.CheckoutSessionResponse;
import com.timerbook.TimerBook.dto.billing.CustomerPortalResponse;
import com.timerbook.TimerBook.dto.billing.SubscriptionResponse;
import com.timerbook.TimerBook.services.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/billing")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @PostMapping("/checkout-session")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CheckoutSessionRequest request
    ) {
        CheckoutSessionResponse resp = billingService.createCheckoutSession(authHeader, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping("/webhook/{provider}")
    public ResponseEntity<String> handleWebhook(
            @PathVariable String provider,
            @RequestBody String payload,
            @RequestHeader Map<String, String> headers
    ) {
        String signature = headers.getOrDefault("Stripe-Signature", headers.getOrDefault("X-Mercadopago-Signature", ""));
        boolean processed = billingService.handleWebhook(provider.toUpperCase(), payload, signature, headers);
        if (processed) return ResponseEntity.ok("received");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ignored");
    }

    @GetMapping("/subscription/me")
    public ResponseEntity<SubscriptionResponse> getMySubscription(
            @RequestHeader("Authorization") String authHeader
    ) {
        SubscriptionResponse resp = billingService.getMySubscription(authHeader);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/customer-portal")
    public ResponseEntity<CustomerPortalResponse> createCustomerPortal(
            @RequestHeader("Authorization") String authHeader
    ) {
        String url = billingService.createCustomerPortal(authHeader);
        return ResponseEntity.ok(new CustomerPortalResponse(url));
    }
}
