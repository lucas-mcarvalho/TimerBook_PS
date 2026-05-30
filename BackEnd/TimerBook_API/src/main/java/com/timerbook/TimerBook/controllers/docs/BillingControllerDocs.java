package com.timerbook.TimerBook.controllers.docs;

import com.timerbook.TimerBook.dto.billing.CheckoutSessionRequest;
import com.timerbook.TimerBook.dto.billing.CheckoutSessionResponse;
import com.timerbook.TimerBook.dto.billing.CustomerPortalResponse;
import com.timerbook.TimerBook.dto.billing.SubscriptionResponse;
import com.timerbook.TimerBook.dto.billing.WebhookEventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Tag(name = "Billing", description = "Fluxos de pagamento, assinatura e webhooks")
public interface BillingControllerDocs {

    @Operation(summary = "Cria uma sessão de checkout", description = "Cria a preferência de pagamento e retorna a URL do checkout do provedor.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Checkout criado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CheckoutSessionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "503", description = "Provedor de pagamento não configurado", content = @Content),
            @ApiResponse(responseCode = "502", description = "Falha ao criar a preferência no provedor", content = @Content)
    })
    ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CheckoutSessionRequest request
    );

    @Operation(summary = "Recebe webhooks do provedor", description = "Recebe notificações do provedor de pagamento, valida a assinatura e processa o evento com idempotência.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook recebido e processado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Webhook inválido ou provider não suportado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Assinatura do webhook inválida ou expirada", content = @Content),
            @ApiResponse(responseCode = "502", description = "Falha ao processar webhook", content = @Content)
    })
    ResponseEntity<String> handleWebhook(
            @PathVariable String provider,
            @RequestBody String payload,
            HttpServletRequest request,
            @RequestHeader Map<String, String> headers
    );

    @Operation(summary = "Lista eventos de webhook salvos", description = "Retorna os eventos persistidos na tabela webhook_events para consulta e depuração.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Eventos retornados com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = WebhookEventResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<List<WebhookEventResponse>> listWebhookEvents(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer limit
    );

    @Operation(summary = "Consulta assinatura do usuário autenticado", description = "Retorna o estado atual da assinatura vinculada ao usuário logado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assinatura retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubscriptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<SubscriptionResponse> getMySubscription(
            @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Retorna a URL do portal do cliente", description = "Retorna a URL configurada para a área de gerenciamento da assinatura.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerPortalResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<CustomerPortalResponse> createCustomerPortal(
            @RequestHeader("Authorization") String authHeader
    );
}