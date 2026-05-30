package com.timerbook.TimerBook.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timerbook.TimerBook.dto.billing.CheckoutSessionRequest;
import com.timerbook.TimerBook.dto.billing.CheckoutSessionResponse;
import com.timerbook.TimerBook.dto.billing.SubscriptionResponse;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.models.billing.PaymentTransaction;
import com.timerbook.TimerBook.models.billing.UserSubscription;
import com.timerbook.TimerBook.models.billing.WebhookEvent;
import com.timerbook.TimerBook.repository.PaymentTransactionRepository;
import com.timerbook.TimerBook.repository.UserRepository;
import com.timerbook.TimerBook.repository.UserSubscriptionRepository;
import com.timerbook.TimerBook.repository.WebhookEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BillingServiceImpl implements BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingServiceImpl.class);
    private static final String PROVIDER_MERCADO_PAGO = "MERCADO_PAGO";

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private WebhookEventRepository webhookEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${payment.provider:MERCADO_PAGO}")
    private String defaultProvider;

    @Value("${payment.mercado-pago.access-token:}")
    private String mercadoPagoAccessToken;

    @Value("${payment.mercado-pago.base-url:https://api.mercadopago.com}")
    private String mercadoPagoBaseUrl;

    @Value("${payment.checkout.title:TimerBook Premium}")
    private String checkoutTitle;

    @Value("${payment.checkout.amount:19.90}")
    private BigDecimal checkoutAmount;

    @Value("${payment.checkout.currency:BRL}")
    private String checkoutCurrency;

    @Value("${payment.checkout.success-url:http://localhost:5173/perfil?payment=success}")
    private String defaultSuccessUrl;

    @Value("${payment.checkout.cancel-url:http://localhost:5173/perfil?payment=cancel}")
    private String defaultCancelUrl;

    @Value("${payment.checkout.notification-url:http://localhost:8080/billing/webhook/mercado_pago}")
    private String notificationUrl;

    @Value("${payment.customer-portal-url:http://localhost:5173/perfil}")
    private String customerPortalUrl;

    @Override
    @Transactional
    public CheckoutSessionResponse createCheckoutSession(String authHeader, CheckoutSessionRequest request) {
        User user = userService.getMe(authHeader);

        if (!PROVIDER_MERCADO_PAGO.equalsIgnoreCase(defaultProvider)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Provider de pagamento não configurado para Mercado Pago.");
        }
        if (mercadoPagoAccessToken == null || mercadoPagoAccessToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "MP access token não configurado. Defina PAYMENT/MP_ACCESS_TOKEN.");
        }

        String successUrl = request.getSuccessUrl() != null && !request.getSuccessUrl().isBlank()
                ? request.getSuccessUrl() : defaultSuccessUrl;
        String cancelUrl = request.getCancelUrl() != null && !request.getCancelUrl().isBlank()
                ? request.getCancelUrl() : defaultCancelUrl;
        boolean useAutoReturn = isPublicHttpsUrl(successUrl);

        Map<String, Object> payload = new HashMap<>();
        payload.put("external_reference", String.valueOf(user.getId()));
        payload.put("notification_url", notificationUrl);
        if (useAutoReturn) {
            payload.put("auto_return", "approved");
        } else {
            logger.warn("Checkout sem auto_return porque successUrl não é pública em HTTPS: {}", successUrl);
        }

        Map<String, String> backUrls = new HashMap<>();
        backUrls.put("success", successUrl);
        backUrls.put("failure", cancelUrl);
        backUrls.put("pending", cancelUrl);
        payload.put("back_urls", backUrls);

        Map<String, Object> payer = new HashMap<>();
        payer.put("email", user.getEmail());
        payload.put("payer", payer);

        Map<String, Object> item = new HashMap<>();
        item.put("title", request.getPlanId() != null && !request.getPlanId().isBlank() ? request.getPlanId() : checkoutTitle);
        item.put("quantity", 1);
        item.put("currency_id", checkoutCurrency);
        item.put("unit_price", checkoutAmount);
        payload.put("items", List.of(item));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(mercadoPagoAccessToken);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                mercadoPagoBaseUrl + "/checkout/preferences",
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                String.class
            );
        } catch (HttpStatusCodeException ex) {
            logger.error("Mercado Pago rejeitou a preferência. status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Mercado Pago rejeitou a criação da preferência: " + safeErrorMessage(ex.getResponseBodyAsString())
            );
        }

        try {
            JsonNode body = objectMapper.readTree(response.getBody());
            String sessionId = body.path("id").asText(null);
            String checkoutUrl = body.path("init_point").asText(null);
            if ((checkoutUrl == null || checkoutUrl.isBlank()) && body.has("sandbox_init_point")) {
                checkoutUrl = body.path("sandbox_init_point").asText(null);
            }
            if (sessionId == null || checkoutUrl == null || checkoutUrl.isBlank()) {
                throw new IllegalStateException("Resposta inválida do Mercado Pago ao criar preferência.");
            }

            upsertUserSubscription(user, sessionId, "PENDING", null, null);
            logger.info("Mercado Pago checkout criado para userId={} preferenceId={}", user.getId(), sessionId);

            return new CheckoutSessionResponse(sessionId, checkoutUrl);
        } catch (Exception e) {
            logger.error("Falha ao processar resposta do Mercado Pago. body={}", response.getBody(), e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha ao processar resposta do Mercado Pago.");
        }
    }

    @Override
    @Transactional
    public boolean handleWebhook(String provider, String payload, String signature, Map<String, String> headers) {
        if (provider == null || !PROVIDER_MERCADO_PAGO.equalsIgnoreCase(provider)) {
            logger.warn("Webhook ignorado para provider não suportado: {}", provider);
            return false;
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String providerEventId = root.path("id").asText();
            if (providerEventId == null || providerEventId.isBlank()) {
                providerEventId = "mp_evt_" + sha256(payload).substring(0, 16);
            }

            if (webhookEventRepository.existsByProviderEventId(providerEventId)) {
                logger.info("Evento Mercado Pago duplicado ignorado: {}", providerEventId);
                return true;
            }

            WebhookEvent event = new WebhookEvent();
            event.setProvider(PROVIDER_MERCADO_PAGO);
            event.setProviderEventId(providerEventId);
            event.setEventType(root.path("type").asText("unknown"));
            event.setPayloadHash(sha256(payload));
            event.setRawPayload(payload);
            event.setProcessingStatus("RECEIVED");
            webhookEventRepository.save(event);

            String type = root.path("type").asText("");
            if (!"payment".equalsIgnoreCase(type)) {
                event.setProcessingStatus("IGNORED");
                event.setProcessedAt(LocalDateTime.now());
                webhookEventRepository.save(event);
                return true;
            }

            String paymentId = root.path("data").path("id").asText(null);
            if (paymentId == null || paymentId.isBlank()) {
                event.setProcessingStatus("FAILED");
                event.setProcessedAt(LocalDateTime.now());
                webhookEventRepository.save(event);
                return false;
            }

            JsonNode payment = fetchMercadoPagoPayment(paymentId);
            processPaymentEvent(payment);

            event.setProcessingStatus("PROCESSED");
            event.setProcessedAt(LocalDateTime.now());
            webhookEventRepository.save(event);
            return true;
        } catch (Exception ex) {
            logger.error("Erro ao processar webhook Mercado Pago", ex);
            return false;
        }
    }

    @Override
    public SubscriptionResponse getMySubscription(String authHeader) {
        User user = userService.getMe(authHeader);
        Optional<UserSubscription> subscriptionOpt = userSubscriptionRepository.findByUserId(user.getId());
        if (subscriptionOpt.isPresent()) {
            UserSubscription subscription = subscriptionOpt.get();
            Instant periodEnd = null;
            if (subscription.getCurrentPeriodEnd() != null) {
                periodEnd = subscription.getCurrentPeriodEnd().toInstant(ZoneOffset.UTC);
            }
            return new SubscriptionResponse(
                    subscription.getProvider(),
                    subscription.getStatus(),
                    periodEnd,
                    subscription.getProviderSubscriptionId()
            );
        }

        String status = "PAID".equals(user.getSubscriptionPlan()) ? "ACTIVE" : "FREE";
        return new SubscriptionResponse(defaultProvider, status, null, null);
    }

    @Override
    public String createCustomerPortal(String authHeader) {
        userService.getMe(authHeader);
        return customerPortalUrl;
    }

    private JsonNode fetchMercadoPagoPayment(String paymentId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(mercadoPagoAccessToken);
        ResponseEntity<String> response = restTemplate.exchange(
                mercadoPagoBaseUrl + "/v1/payments/" + paymentId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        return objectMapper.readTree(response.getBody());
    }

    private void processPaymentEvent(JsonNode payment) {
        String paymentId = payment.path("id").asText();
        String status = payment.path("status").asText("pending");
        String externalReference = payment.path("external_reference").asText("");

        if (externalReference.isBlank()) {
            logger.warn("Pagamento {} sem external_reference; ignorando atualização de plano.", paymentId);
            return;
        }

        Long userId = Long.parseLong(externalReference);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado para external_reference: " + externalReference));

        PaymentTransaction transaction = paymentTransactionRepository
                .findByProviderPaymentId(paymentId)
                .orElseGet(PaymentTransaction::new);

        transaction.setUser(user);
        transaction.setProvider(PROVIDER_MERCADO_PAGO);
        transaction.setProviderPaymentId(paymentId);
        transaction.setTransactionType(resolveTransactionType(user.getId()));
        transaction.setAmount(payment.path("transaction_amount").decimalValue());
        transaction.setCurrency(payment.path("currency_id").asText("BRL"));
        transaction.setStatus(mapPaymentStatus(status));
        transaction.setRawPayload(payment.toString());

        String approvedDate = payment.path("date_approved").asText(null);
        if (approvedDate != null && !approvedDate.isBlank()) {
            transaction.setPaidAt(OffsetDateTime.parse(approvedDate).toLocalDateTime());
        }

        paymentTransactionRepository.save(transaction);

        if ("approved".equalsIgnoreCase(status)) {
            user.setSubscriptionPlan("PAID");
            userRepository.save(user);
            upsertUserSubscription(
                    user,
                    payment.path("order").path("id").asText("payment-" + paymentId),
                    "ACTIVE",
                    LocalDateTime.now().plusDays(30),
                    null
            );
            return;
        }

        if ("cancelled".equalsIgnoreCase(status) || "rejected".equalsIgnoreCase(status) || "refunded".equalsIgnoreCase(status)) {
            user.setSubscriptionPlan("FREE");
            userRepository.save(user);
            upsertUserSubscription(
                    user,
                    payment.path("order").path("id").asText("payment-" + paymentId),
                    "CANCELED",
                    null,
                    LocalDateTime.now()
            );
        }
    }

    private String resolveTransactionType(Long userId) {
        return userSubscriptionRepository.findByUserId(userId).isPresent() ? "RENEWAL" : "FIRST_PAYMENT";
    }

    private String mapPaymentStatus(String providerStatus) {
        if (providerStatus == null) {
            return "PENDING";
        }
        return switch (providerStatus.toLowerCase()) {
            case "approved" -> "SUCCEEDED";
            case "refunded", "charged_back" -> "REFUNDED";
            case "cancelled" -> "CANCELED";
            case "rejected" -> "FAILED";
            default -> "PENDING";
        };
    }

    private void upsertUserSubscription(
            User user,
            String providerSubscriptionId,
            String status,
            LocalDateTime currentPeriodEnd,
            LocalDateTime canceledAt
    ) {
        UserSubscription subscription = userSubscriptionRepository.findByUserId(user.getId())
                .orElseGet(UserSubscription::new);

        subscription.setUser(user);
        subscription.setProvider(PROVIDER_MERCADO_PAGO);
        subscription.setProviderSubscriptionId(providerSubscriptionId);
        subscription.setStatus(status);
        subscription.setCurrentPeriodEnd(currentPeriodEnd);
        subscription.setCanceledAt(canceledAt);
        subscription.setUpdatedAt(LocalDateTime.now());
        if (subscription.getCreatedAt() == null) {
            subscription.setCreatedAt(LocalDateTime.now());
        }

        userSubscriptionRepository.save(subscription);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular hash", e);
        }
    }

    private boolean isPublicHttpsUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        String normalized = url.trim().toLowerCase();
        if (!normalized.startsWith("https://")) {
            return false;
        }

        return !(normalized.contains("localhost") || normalized.contains("127.0.0.1") || normalized.contains("0.0.0.0"));
    }

    private String safeErrorMessage(String body) {
        if (body == null || body.isBlank()) {
            return "sem detalhes";
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.has("message")) {
                return node.path("message").asText();
            }
            if (node.has("error")) {
                return node.path("error").asText();
            }
        } catch (Exception ignored) {
            // fallback below
        }
        return body.length() > 180 ? body.substring(0, 180) + "..." : body;
    }
}
