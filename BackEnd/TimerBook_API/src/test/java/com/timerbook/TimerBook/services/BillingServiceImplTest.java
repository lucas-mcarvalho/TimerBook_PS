package com.timerbook.TimerBook.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BillingServiceImplTest {

    private static final String WEBHOOK_SECRET = "unit-test-webhook-secret";

    private BillingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BillingServiceImpl();
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(service, "paymentWebhookSecret", WEBHOOK_SECRET);
        ReflectionTestUtils.setField(service, "webhookSignatureValidationEnabled", true);
    }

    @Test
    void validateMercadoPagoWebhookSignatureShouldAcceptMillisecondTimestampFromQueryDataId() {
        String payload = """
                {
                  "action": "payment.updated",
                  "api_version": "v1",
                  "data": {
                    "id": "123456"
                  },
                  "id": "123456",
                  "type": "payment"
                }
                """;
        String requestId = "bb56a2f1-6aae-46ac-982e-9dcd3581d08e";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String manifest = "id:123456;request-id:" + requestId + ";ts:" + timestamp + ";";

        Map<String, String> headers = new HashMap<>();
        headers.put("x-request-id", requestId);
        headers.put("x-signature", "ts=" + timestamp + ",v1=" + hmacSha256Hex(manifest, WEBHOOK_SECRET));
        headers.put("__query_string__", "data.id=123456&type=payment");

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                service,
                "validateMercadoPagoWebhookSignature",
                payload,
                headers
        ));
    }

    @Test
    void validateMercadoPagoWebhookSignatureShouldAcceptLegacyIdQueryParam() {
        String payload = """
                {
                  "resource": "/v1/payments/999999",
                  "topic": "payment"
                }
                """;
        String requestId = "5b60f0d8-8a84-49c3-8b7f-bd7acdf09868";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String manifest = "id:abc123;request-id:" + requestId + ";ts:" + timestamp + ";";

        Map<String, String> headers = new HashMap<>();
        headers.put("x-request-id", requestId);
        headers.put("x-signature", "ts=" + timestamp + ",v1=" + hmacSha256Hex(manifest, WEBHOOK_SECRET));
        headers.put("__query_string__", "id=ABC123&topic=payment");

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                service,
                "validateMercadoPagoWebhookSignature",
                payload,
                headers
        ));
    }

    @Test
    void validateMercadoPagoWebhookSignatureShouldAcceptManifestWithoutIdWhenUrlHasNoQueryId() {
        String payload = """
                {
                  "resource": "https://api.mercadolibre.com/merchant_orders/41349283225",
                  "topic": "merchant_order"
                }
                """;
        String requestId = "c4ffaa19-972c-4b1d-acc2-3f4e88bdf794";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
        String manifest = "request-id:" + requestId + ";ts:" + timestamp + ";";

        Map<String, String> headers = new HashMap<>();
        headers.put("x-request-id", requestId);
        headers.put("x-signature", "ts=" + timestamp + ",v1=" + hmacSha256Hex(manifest, WEBHOOK_SECRET));
        headers.put("__query_string__", "");

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                service,
                "validateMercadoPagoWebhookSignature",
                payload,
                headers
        ));
    }

    @Test
    void ensureWebhookNotificationUrlShouldForceMercadoPagoWebhookMode() {
        assertEquals(
                "https://example.ngrok-free.app/billing/webhook/mercado_pago?source_news=webhooks",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "ensureWebhookNotificationUrl",
                        "https://example.ngrok-free.app/billing/webhook/mercado_pago"
                )
        );
        assertEquals(
                "https://example.ngrok-free.app/billing/webhook/mercado_pago?foo=bar&source_news=webhooks",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "ensureWebhookNotificationUrl",
                        "https://example.ngrok-free.app/billing/webhook/mercado_pago?foo=bar"
                )
        );
    }

    @Test
    void validateMercadoPagoWebhookSignatureShouldSkipWhenValidationIsDisabled() {
        ReflectionTestUtils.setField(service, "webhookSignatureValidationEnabled", false);

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                service,
                "validateMercadoPagoWebhookSignature",
                "{}",
                Map.of()
        ));
    }

    private String hmacSha256Hex(String value, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : rawHmac) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
