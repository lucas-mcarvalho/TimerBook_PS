package com.timerbook.TimerBook.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timerbook.TimerBook.dto.billing.CheckoutSessionRequest;
import com.timerbook.TimerBook.dto.billing.CheckoutSessionResponse;
import com.timerbook.TimerBook.dto.billing.SubscriptionResponse;
import com.timerbook.TimerBook.dto.billing.WebhookEventResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.GeneralSecurityException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class BillingServiceImpl implements BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingServiceImpl.class);
    private static final String PROVIDER_MERCADO_PAGO = "MERCADO_PAGO";
    private static final Pattern WEBHOOK_SIGNATURE_PATTERN = Pattern.compile("(?i)(ts|v1)=([^,]+)");
    private static final long WEBHOOK_SKEW_MILLIS = 300_000L;
    private static final long TIMESTAMP_SECONDS_THRESHOLD = 100_000_000_000L;

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

    @Value("${payment.webhook-secret:}")
    private String paymentWebhookSecret;

    @Value("${payment.webhook-signature.enabled:true}")
    private boolean webhookSignatureValidationEnabled;

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
        payload.put("notification_url", ensureWebhookNotificationUrl(notificationUrl));
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
            validateMercadoPagoWebhookSignature(payload, headers);
            JsonNode root = objectMapper.readTree(payload);
            String providerEventId = getHeaderIgnoreCase(headers, "x-request-id");
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
            String eventType = resolveWebhookEventType(root);
            event.setEventType(eventType);
            event.setPayloadHash(sha256(payload));
            event.setRawPayload(payload);
            event.setProcessingStatus("RECEIVED");
            webhookEventRepository.save(event);

            String resourceId = resolveWebhookResourceId(root);
            if (isPaymentWebhookType(eventType)) {
                if (resourceId == null || resourceId.isBlank()) {
                    event.setProcessingStatus("FAILED");
                    event.setProcessedAt(LocalDateTime.now());
                    webhookEventRepository.save(event);
                    return false;
                }

                JsonNode payment = fetchMercadoPagoPayment(resourceId);
                processPaymentEvent(payment);
            } else if (isMerchantOrderWebhookType(eventType, root)) {
                if (resourceId == null || resourceId.isBlank()) {
                    event.setProcessingStatus("FAILED");
                    event.setProcessedAt(LocalDateTime.now());
                    webhookEventRepository.save(event);
                    return false;
                }

                JsonNode merchantOrder = fetchMercadoPagoMerchantOrder(resourceId);
                processMerchantOrderEvent(merchantOrder);
            } else {
                logger.info("Webhook Mercado Pago ignorado por tipo não tratado: {}", eventType);
                event.setProcessingStatus("IGNORED");
                event.setProcessedAt(LocalDateTime.now());
                webhookEventRepository.save(event);
                return true;
            }

            event.setProcessingStatus("PROCESSED");
            event.setProcessedAt(LocalDateTime.now());
            webhookEventRepository.save(event);
            return true;
        } catch (ResponseStatusException ex) {
            logger.warn("Webhook Mercado Pago rejeitado: status={} reason={}", ex.getStatusCode(), ex.getReason());
            throw ex;
        } catch (HttpStatusCodeException ex) {
            logger.error("Falha ao consultar Mercado Pago durante webhook. status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha ao consultar Mercado Pago durante webhook.");
        } catch (Exception ex) {
            logger.error("Erro ao processar webhook Mercado Pago", ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Erro ao processar webhook Mercado Pago.");
        }
    }

    @Override
    public List<WebhookEventResponse> listWebhookEvents(Integer limit) {
        int effectiveLimit = limit == null || limit <= 0 ? 20 : Math.min(limit, 100);
        return webhookEventRepository.findAll(PageRequest.of(0, effectiveLimit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(event -> new WebhookEventResponse(
                        event.getId(),
                        event.getProvider(),
                        event.getProviderEventId(),
                        event.getEventType(),
                        event.getProcessingStatus(),
                        event.getPayloadHash(),
                        event.getRawPayload(),
                        event.getProcessedAt(),
                        event.getCreatedAt()
                ))
                .toList();
    }

    private JsonNode fetchMercadoPagoMerchantOrder(String merchantOrderId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(mercadoPagoAccessToken);
        ResponseEntity<String> response = restTemplate.exchange(
                mercadoPagoBaseUrl + "/merchant_orders/" + merchantOrderId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        return objectMapper.readTree(response.getBody());
    }

    private void processMerchantOrderEvent(JsonNode merchantOrder) {
        String merchantOrderId = merchantOrder.path("id").asText("");
        JsonNode payments = merchantOrder.path("payments");

        if (payments.isArray()) {
            for (JsonNode paymentRef : payments) {
                String paymentRefId = paymentRef.path("id").asText("");
                if (paymentRefId.isBlank()) {
                    continue;
                }

                try {
                    JsonNode payment = fetchMercadoPagoPayment(paymentRefId);
                    processPaymentEvent(payment);
                    if ("approved".equalsIgnoreCase(payment.path("status").asText(""))) {
                        return;
                    }
                } catch (Exception ex) {
                    logger.warn("Falha ao processar payment {} encontrado em merchant order {}", paymentRefId, merchantOrderId, ex);
                }
            }
        }

        String orderStatus = merchantOrder.path("status").asText("");
        if (isPaidMerchantOrderStatus(orderStatus)) {
            String paymentId = merchantOrder.path("payment_id").asText(null);
            if (paymentId != null && !paymentId.isBlank()) {
                try {
                    JsonNode payment = fetchMercadoPagoPayment(paymentId);
                    processPaymentEvent(payment);
                    return;
                } catch (Exception ex) {
                    throw new IllegalStateException("Falha ao processar merchant order paga " + merchantOrderId, ex);
                }
            }
        }

        logger.info("Merchant order {} sem pagamento aprovado para processar.", merchantOrderId);
    }

    private String resolveWebhookEventType(JsonNode root) {
        String type = root.path("type").asText("");
        if (type == null || type.isBlank()) {
            type = root.path("topic").asText("");
        }
        if (type == null || type.isBlank()) {
            type = root.path("action").asText("");
        }
        return type == null || type.isBlank() ? "unknown" : type;
    }

    private String resolveWebhookResourceId(JsonNode root) {
        String dataId = root.path("data").path("id").asText(null);
        if (dataId != null && !dataId.isBlank()) {
            return dataId;
        }

        String rootId = root.path("id").asText(null);
        if (rootId != null && !rootId.isBlank()) {
            return rootId;
        }

        String resource = root.path("resource").asText(null);
        if (resource != null && !resource.isBlank()) {
            int lastSlash = resource.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < resource.length() - 1) {
                return resource.substring(lastSlash + 1);
            }
            return resource;
        }

        return null;
    }

    private boolean isPaymentWebhookType(String eventType) {
        if (eventType == null) {
            return false;
        }
        String normalized = eventType.trim().toLowerCase();
        return "payment".equals(normalized) || "payments".equals(normalized);
    }

    private boolean isMerchantOrderWebhookType(String eventType, JsonNode root) {
        String normalized = eventType == null ? "" : eventType.trim().toLowerCase();
        if (normalized.contains("merchant_order") || normalized.contains("merchant_orders")) {
            return true;
        }

        String resource = root.path("resource").asText("");
        return resource.contains("merchant_orders");
    }

    private boolean isPaidMerchantOrderStatus(String status) {
        if (status == null) {
            return false;
        }

        return switch (status.trim().toLowerCase()) {
            case "paid", "closed", "completed" -> true;
            default -> false;
        };
    }

    private void validateMercadoPagoWebhookSignature(String payload, Map<String, String> headers) {
        if (!webhookSignatureValidationEnabled) {
            logger.warn("Validação de assinatura do webhook Mercado Pago desabilitada por configuração.");
            return;
        }

        if (paymentWebhookSecret == null || paymentWebhookSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "PAYMENT_WEBHOOK_SECRET não configurado.");
        }

        String xSignature = getHeaderIgnoreCase(headers, "x-signature");
        String xRequestId = getHeaderIgnoreCase(headers, "x-request-id");
        if (xSignature == null || xSignature.isBlank() || xRequestId == null || xRequestId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cabeçalhos x-signature e x-request-id são obrigatórios.");
        }
        xRequestId = xRequestId.trim();

        JsonNode root;
        try {
            root = objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload inválido do webhook.");
        }

        Set<String> dataIds = extractWebhookIdentifierCandidates(root, headers);

        Map<String, String> signatureParts = parseWebhookSignature(xSignature);
        String ts = signatureParts.get("ts");
        String receivedHash = signatureParts.get("v1");
        if (ts == null || receivedHash == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "x-signature inválido.");
        }

        long timestampMillis;
        try {
            timestampMillis = parseMercadoPagoTimestampMillis(ts);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Timestamp do webhook inválido.");
        }

        long nowMillis = System.currentTimeMillis();
        if (Math.abs(nowMillis - timestampMillis) > WEBHOOK_SKEW_MILLIS) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Webhook expirado.");
        }

        for (String dataId : dataIds) {
            String manifest = "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts + ";";
            String expectedHash = hmacSha256Hex(manifest, paymentWebhookSecret);
            if (constantTimeEqualsHex(expectedHash, receivedHash)) {
                return;
            }
        }
        String manifestWithoutId = "request-id:" + xRequestId + ";ts:" + ts + ";";
        String expectedHashWithoutId = hmacSha256Hex(manifestWithoutId, paymentWebhookSecret);
        if (constantTimeEqualsHex(expectedHashWithoutId, receivedHash)) {
            return;
        }

        logger.warn(
                "Assinatura inválida do Mercado Pago. dataIds={} triedWithoutId=true xRequestId={} ts={} receivedHashPrefix={}",
                dataIds,
                xRequestId,
                ts,
                abbreviateHash(receivedHash)
        );
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Assinatura do webhook inválida.");
    }

    private Set<String> extractWebhookIdentifierCandidates(JsonNode root, Map<String, String> headers) {
        Set<String> candidates = new LinkedHashSet<>();
        String queryString = getHeaderIgnoreCase(headers, "__query_string__");
        addWebhookIdentifierCandidate(candidates, extractQueryParam(queryString, "data.id"));
        addWebhookIdentifierCandidate(candidates, extractQueryParam(queryString, "id"));
        addWebhookIdentifierCandidate(candidates, extractQueryParam(queryString, "data_id"));
        addWebhookIdentifierCandidate(candidates, extractQueryParam(queryString, "data.id_url"));

        addWebhookIdentifierCandidate(candidates, root.path("data").path("id_url").asText(null));
        addWebhookIdentifierCandidate(candidates, root.path("data").path("id").asText(null));
        addWebhookIdentifierCandidate(candidates, root.path("id").asText(null));

        String resource = root.path("resource").asText(null);
        addWebhookIdentifierCandidate(candidates, resource);

        if (resource != null && !resource.isBlank()) {
            int lastSlash = resource.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < resource.length() - 1) {
                addWebhookIdentifierCandidate(candidates, resource.substring(lastSlash + 1));
            }
        }

        return candidates;
    }

    private void addWebhookIdentifierCandidate(Set<String> candidates, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        candidates.add(value.trim().toLowerCase(Locale.ROOT));
    }

    private String extractQueryParam(String queryString, String key) {
        if (queryString == null || queryString.isBlank() || key == null || key.isBlank()) {
            return null;
        }

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int equalsIndex = pair.indexOf('=');
            if (equalsIndex <= 0) {
                continue;
            }

            String currentKey = URLDecoder.decode(pair.substring(0, equalsIndex), StandardCharsets.UTF_8);
            if (!key.equals(currentKey)) {
                continue;
            }

            String value = pair.substring(equalsIndex + 1);
            return value == null ? null : URLDecoder.decode(value, StandardCharsets.UTF_8).trim();
        }

        return null;
    }

    private long parseMercadoPagoTimestampMillis(String ts) {
        long parsedTimestamp = Long.parseLong(ts);
        if (parsedTimestamp < TIMESTAMP_SECONDS_THRESHOLD) {
            return parsedTimestamp * 1000L;
        }
        return parsedTimestamp;
    }

    private boolean constantTimeEqualsHex(String expectedHash, String receivedHash) {
        if (expectedHash == null || receivedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expectedHash.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8),
                receivedHash.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8)
        );
    }

    private String abbreviateHash(String hash) {
        if (hash == null || hash.isBlank()) {
            return "";
        }
        String trimmed = hash.trim();
        return trimmed.length() <= 8 ? trimmed : trimmed.substring(0, 8);
    }

    private Map<String, String> parseWebhookSignature(String xSignature) {
        Map<String, String> values = new HashMap<>();
        Matcher matcher = WEBHOOK_SIGNATURE_PATTERN.matcher(xSignature);
        while (matcher.find()) {
            values.put(matcher.group(1).toLowerCase(), matcher.group(2).trim());
        }
        return values;
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
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Erro ao validar assinatura do webhook.", ex);
        }
    }

    private String getHeaderIgnoreCase(Map<String, String> headers, String name) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
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

    private String ensureWebhookNotificationUrl(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }

        String normalized = url.trim();
        if (normalized.contains("source_news=")) {
            return normalized;
        }

        String separator = normalized.contains("?") ? "&" : "?";
        return normalized + separator + "source_news=webhooks";
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
