package com.payment.gateway.application.webhook.port.out;

/**
 * Output port for delivering integration events to merchant webhook endpoints.
 */
public interface WebhookDeliveryPort {

    /**
     * Deliver an event payload to the merchant's registered webhook URL.
     * Implementations must be non-blocking failures: delivery problems are
     * logged/audited but never propagated to the caller.
     *
     * @param merchantId  the merchant to notify
     * @param eventType   the event type (e.g. PAYMENT_COMPLETED)
     * @param payloadJson the raw event payload as JSON
     */
    void deliver(String merchantId, String eventType, String payloadJson);
}
