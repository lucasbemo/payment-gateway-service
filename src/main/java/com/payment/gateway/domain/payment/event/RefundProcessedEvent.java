package com.payment.gateway.domain.payment.event;

import com.payment.gateway.commons.event.IntegrationEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event published when a refund is processed.
 * Supports schema versioning for backward compatibility.
 */
@Getter
public class RefundProcessedEvent extends IntegrationEvent {

    public static final String CURRENT_SCHEMA_VERSION = "1.0.0";

    private final String paymentId;
    private final String merchantId;
    private final String refundAmount;
    private final String currency;
    private final String refundType;

    public RefundProcessedEvent(String aggregateId, String paymentId, String merchantId,
                                 String refundAmount, String currency, String refundType) {
        super(aggregateId, CURRENT_SCHEMA_VERSION, "REFUND_PROCESSED");
        this.paymentId = paymentId;
        this.merchantId = merchantId;
        this.refundAmount = refundAmount;
        this.currency = currency;
        this.refundType = refundType;
    }

    public RefundProcessedEvent(String id, Instant occurredOn, String aggregateId,
                                 String paymentId, String merchantId, String refundAmount,
                                 String currency, String refundType, String schemaVersion) {
        super(id, occurredOn, aggregateId, schemaVersion, "REFUND_PROCESSED");
        this.paymentId = paymentId;
        this.merchantId = merchantId;
        this.refundAmount = refundAmount;
        this.currency = currency;
        this.refundType = refundType;
    }

    @Override
    protected void populateMap(Map<String, Object> map) {
        map.put("paymentId", paymentId);
        map.put("merchantId", merchantId);
        map.put("refundAmount", refundAmount);
        map.put("currency", currency);
        map.put("refundType", refundType);
    }

    @SuppressWarnings("unchecked")
    public static RefundProcessedEvent fromMap(Map<String, Object> map) {
        String schemaVersion = (String) map.get("schemaVersion");
        if (schemaVersion == null) {
            schemaVersion = CURRENT_SCHEMA_VERSION;
        }

        RefundProcessedEvent event = new RefundProcessedEvent(
            (String) map.get("id"),
            (Instant) map.get("occurredOn"),
            (String) map.get("aggregateId"),
            (String) map.get("paymentId"),
            (String) map.get("merchantId"),
            (String) map.get("refundAmount"),
            (String) map.get("currency"),
            (String) map.get("refundType"),
            schemaVersion
        );

        if (!CURRENT_SCHEMA_VERSION.equals(schemaVersion)) {
            event = (RefundProcessedEvent) event.migrateFrom(schemaVersion);
        }

        return event;
    }

    @Override
    public RefundProcessedEvent migrateFrom(String fromVersion) {
        if (fromVersion.startsWith("1.")) {
            return this;
        }
        throw new IllegalArgumentException("Unsupported schema version: " + fromVersion);
    }
}
