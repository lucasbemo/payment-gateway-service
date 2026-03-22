package com.payment.gateway.infrastructure.webhook.adapter.in.rest;

import com.payment.gateway.application.webhook.port.in.WebhookProcessingPort;
import com.payment.gateway.infrastructure.commons.rest.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Webhook endpoints for payment providers")
public class StripeWebhookController {

    private final WebhookProcessingPort webhookProcessingPort;

    @PostMapping("/stripe")
    @Operation(summary = "Handle Stripe webhook", description = "Receives and processes webhook events from Stripe")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid webhook payload or signature"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<WebhookResponse>> handleStripeWebhook(
            @Parameter(description = "Stripe signature header", required = true)
            @RequestHeader("Stripe-Signature") String signature,
            @Parameter(description = "Webhook payload", required = true)
            @RequestBody String payload) {

        log.info("Received Stripe webhook");

        Map<String, String> headers = new HashMap<>();
        headers.put("Stripe-Signature", signature);

        WebhookProcessingPort.WebhookProcessingResult result = webhookProcessingPort.processWebhook(
                "stripe", payload, headers);

        if (!result.processed()) {
            log.warn("Webhook processing failed: {}", result.errorMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(result.errorMessage()));
        }

        log.info("Stripe webhook processed successfully: type={}, entityId={}",
                result.eventType(), result.entityId());

        WebhookResponse response = new WebhookResponse(
                result.eventType(),
                result.entityId(),
                "processed"
        );

        return ResponseEntity.ok(ApiResponse.success("Webhook processed", response));
    }

    @GetMapping("/health")
    @Operation(summary = "Webhook health check", description = "Checks webhook handler health status")
    public ResponseEntity<ApiResponse<HealthResponse>> healthCheck() {
        boolean healthy = webhookProcessingPort.isHealthy();
        String providerName = webhookProcessingPort.getProviderName();

        HealthResponse response = new HealthResponse(providerName, healthy);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    public record WebhookResponse(
            String eventType,
            String entityId,
            String status
    ) {}

    public record HealthResponse(
            String provider,
            boolean healthy
    ) {}
}