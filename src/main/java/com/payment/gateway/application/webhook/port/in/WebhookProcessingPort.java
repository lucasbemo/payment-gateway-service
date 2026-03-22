package com.payment.gateway.application.webhook.port.in;

import java.util.Map;

public interface WebhookProcessingPort {

    WebhookProcessingResult processWebhook(
        String providerName,
        String payload,
        Map<String, String> headers
    );

    boolean verifySignature(
        String providerName,
        String payload,
        String signature,
        String secret
    );

    String getProviderName();

    boolean isHealthy();

    record WebhookProcessingResult(
        boolean processed,
        String eventType,
        String entityId,
        String errorMessage
    ) {}
}