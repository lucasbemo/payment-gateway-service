package com.payment.gateway.infrastructure.webhook.adapter.in.handler;

import com.payment.gateway.application.webhook.port.in.WebhookProcessingPort;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;

@Slf4j
public class StripeWebhookHandler implements WebhookProcessingPort {

    private final String webhookSecret;

    public StripeWebhookHandler(String webhookSecret) {
        this.webhookSecret = webhookSecret;
        log.info("StripeWebhookHandler initialized");
    }

    @Override
    public WebhookProcessingResult processWebhook(String providerName, String payload, Map<String, String> headers) {
        if (!"stripe".equalsIgnoreCase(providerName)) {
            return new WebhookProcessingResult(false, null, null, "Unknown provider: " + providerName);
        }

        try {
            String sigHeader = headers.get("Stripe-Signature");
            if (sigHeader == null || sigHeader.isEmpty()) {
                log.error("Missing Stripe-Signature header");
                return new WebhookProcessingResult(false, null, null, "Missing signature header");
            }

            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            
            String eventType = event.getType();
            String entityId = extractEntityId(event);
            
            log.info("Stripe webhook processed: type={}, id={}, entityId={}", eventType, event.getId(), entityId);
            
            return new WebhookProcessingResult(true, eventType, entityId, null);
            
        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature: {}", e.getMessage());
            return new WebhookProcessingResult(false, null, null, "Invalid signature");
        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage(), e);
            return new WebhookProcessingResult(false, null, null, e.getMessage());
        }
    }

    @Override
    public boolean verifySignature(String providerName, String payload, String signature, String secret) {
        if (!"stripe".equalsIgnoreCase(providerName)) {
            return false;
        }
        try {
            Webhook.constructEvent(payload, signature, secret);
            return true;
        } catch (SignatureVerificationException e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "STRIPE";
    }

    @Override
    public boolean isHealthy() {
        return webhookSecret != null && !webhookSecret.isEmpty();
    }

    private String extractEntityId(Event event) {
        if (event.getData() == null || event.getData().getObject() == null) {
            return null;
        }
        
        var obj = event.getData().getObject();
        if (obj instanceof com.stripe.model.PaymentIntent pi) {
            return pi.getId();
        } else if (obj instanceof com.stripe.model.Refund refund) {
            return refund.getId();
        } else if (obj instanceof com.stripe.model.Customer customer) {
            return customer.getId();
        } else if (obj instanceof com.stripe.model.Charge charge) {
            return charge.getId();
        }
        
        return null;
    }
}