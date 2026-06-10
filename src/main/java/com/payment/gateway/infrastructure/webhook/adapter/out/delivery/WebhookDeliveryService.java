package com.payment.gateway.infrastructure.webhook.adapter.out.delivery;

import com.payment.gateway.application.commons.port.out.AuditPort;
import com.payment.gateway.application.payment.port.out.MerchantQueryPort;
import com.payment.gateway.application.webhook.port.out.WebhookDeliveryPort;
import com.payment.gateway.domain.merchant.model.Merchant;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Outbound adapter that delivers integration events to merchant webhook endpoints.
 *
 * <p>Delivery semantics:
 * <ul>
 *   <li>Merchants without a registered webhook URL are skipped silently (debug log).</li>
 *   <li>Up to {@code webhook.delivery.max-attempts} attempts (default 3) with linear
 *       backoff (1s, 2s by default) on connectivity errors (IOException) and 5xx responses.</li>
 *   <li>4xx responses are treated as permanent failures (no retry).</li>
 *   <li>Failures are never propagated to the caller — Kafka consumption must not break
 *       because a merchant endpoint is down. Outcomes are logged and audited.</li>
 * </ul>
 */
@Slf4j
@Component
public class WebhookDeliveryService implements WebhookDeliveryPort {

    private static final String HEADER_WEBHOOK_EVENT = "X-Webhook-Event";
    private static final String HEADER_WEBHOOK_ID = "X-Webhook-Id";

    private final MerchantQueryPort merchantQueryPort;
    private final AuditPort auditPort;
    private final RestTemplate restTemplate;
    private final int maxAttempts;
    private final long baseBackoffMs;

    public WebhookDeliveryService(
            MerchantQueryPort merchantQueryPort,
            AuditPort auditPort,
            @Qualifier("webhookRestTemplate") RestTemplate restTemplate,
            @Value("${webhook.delivery.max-attempts:3}") int maxAttempts,
            @Value("${webhook.delivery.base-backoff-ms:1000}") long baseBackoffMs) {
        this.merchantQueryPort = merchantQueryPort;
        this.auditPort = auditPort;
        this.restTemplate = restTemplate;
        this.maxAttempts = maxAttempts;
        this.baseBackoffMs = baseBackoffMs;
    }

    @Override
    public void deliver(String merchantId, String eventType, String payloadJson) {
        try {
            doDeliver(merchantId, eventType, payloadJson);
        } catch (Exception e) {
            // Webhook delivery must never break event consumption.
            log.error(
                    "Unexpected error during webhook delivery: merchantId={}, eventType={}", merchantId, eventType, e);
        }
    }

    private void doDeliver(String merchantId, String eventType, String payloadJson) {
        if (merchantId == null || merchantId.isBlank()) {
            log.debug("Skipping webhook delivery: no merchantId on event (eventType={})", eventType);
            return;
        }

        Optional<Merchant> merchant = merchantQueryPort.findById(merchantId);
        if (merchant.isEmpty()) {
            log.debug(
                    "Skipping webhook delivery: merchant not found (merchantId={}, eventType={})",
                    merchantId,
                    eventType);
            return;
        }

        String webhookUrl = merchant.get().getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.debug(
                    "Skipping webhook delivery: merchant has no webhookUrl (merchantId={}, eventType={})",
                    merchantId,
                    eventType);
            return;
        }

        String webhookId = UUID.randomUUID().toString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HEADER_WEBHOOK_EVENT, eventType);
        headers.set(HEADER_WEBHOOK_ID, webhookId);
        HttpEntity<String> request = new HttpEntity<>(payloadJson, headers);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);
                log.info(
                        "Webhook delivered: merchantId={}, eventType={}, webhookId={}, status={}, attempt={}/{}",
                        merchantId,
                        eventType,
                        webhookId,
                        response.getStatusCode().value(),
                        attempt,
                        maxAttempts);
                auditPort.logWebhookDelivery(merchantId, eventType, webhookUrl, "SUCCESS", attempt);
                return;
            } catch (ResourceAccessException | HttpServerErrorException e) {
                // I/O error or 5xx: retryable
                log.warn(
                        "Webhook delivery attempt {}/{} failed: merchantId={}, eventType={}, webhookId={}, error={}",
                        attempt,
                        maxAttempts,
                        merchantId,
                        eventType,
                        webhookId,
                        e.getMessage());
                if (attempt < maxAttempts && !backoff(baseBackoffMs * attempt)) {
                    break; // interrupted: stop retrying
                }
            } catch (RestClientException e) {
                // 4xx or other non-retryable client error: permanent failure
                log.error(
                        "Webhook delivery permanently failed (non-retryable): merchantId={}, eventType={}, "
                                + "webhookId={}, error={}",
                        merchantId,
                        eventType,
                        webhookId,
                        e.getMessage());
                auditPort.logWebhookDelivery(merchantId, eventType, webhookUrl, "FAILED", attempt);
                return;
            }
        }

        log.error(
                "Webhook delivery failed after {} attempts: merchantId={}, eventType={}, webhookId={}",
                maxAttempts,
                merchantId,
                eventType,
                webhookId);
        auditPort.logWebhookDelivery(merchantId, eventType, webhookUrl, "FAILED", maxAttempts);
    }

    /**
     * @return true if the backoff completed, false if the thread was interrupted
     */
    private boolean backoff(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
