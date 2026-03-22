package com.payment.gateway.infrastructure.webhook.adapter.in.handler;

import com.payment.gateway.application.webhook.port.in.WebhookProcessingPort;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class StubWebhookHandler implements WebhookProcessingPort {

    @Override
    public WebhookProcessingResult processWebhook(String providerName, String payload, Map<String, String> headers) {
        log.info("StubWebhookHandler.processWebhook: providerName={}", providerName);
        
        return new WebhookProcessingResult(
                true,
                "stub.event.type",
                "stub-entity-id",
                null
        );
    }

    @Override
    public boolean verifySignature(String providerName, String payload, String signature, String secret) {
        log.info("StubWebhookHandler.verifySignature: providerName={}", providerName);
        return true;
    }

    @Override
    public String getProviderName() {
        return "STUB";
    }

    @Override
    public boolean isHealthy() {
        return true;
    }
}