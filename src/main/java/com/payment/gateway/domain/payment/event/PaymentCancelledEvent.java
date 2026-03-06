package com.payment.gateway.domain.payment.event;

import com.payment.gateway.commons.event.IntegrationEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event published when a payment is cancelled.
 * Supports schema versioning for backward compatibility.
 */
@Getter
public class PaymentCancelledEvent extends IntegrationEvent {

    public static final String CURRENT_SCHEMA_VERSION = "1.0.0";

    private final String merchantId;
    private final String reason;

    public PaymentCancelledEvent(String aggregateId, String merchantId, String reason) {
        super(aggregateId, CURRENT_SCHEMA_VERSION, "PAYMENT_CANCELLED");
        this.merchantId = merchantId;
        this.reason = reason;
    }

    public PaymentCancelledEvent(String id, Instant occurredOn, String aggregateId,
                                  String merchantId, String reason, String schemaVersion) {
        super(id, occurredOn, aggregateId, schemaVersion, "PAYMENT_CANCELLED");
        this.merchantId = merchantId;
        this.reason = reason;
    }

    @Override
    protected void populateMap(Map<String, Object> map) {
        map.put("merchantId", merchantId);
        map.put("reason", reason);
    }

    @SuppressWarnings("unchecked")
    public static PaymentCancelledEvent fromMap(Map<String, Object> map) {
        String schemaVersion = (String) map.get("schemaVersion");
        if (schemaVersion == null) {
            schemaVersion = CURRENT_SCHEMA_VERSION;
        }

        PaymentCancelledEvent event = new PaymentCancelledEvent(
            (String) map.get("id"),
            (Instant) map.get("occurredOn"),
            (String) map.get("aggregateId"),
            (String) map.get("merchantId"),
            (String) map.get("reason"),
            schemaVersion
        );

        if (!CURRENT_SCHEMA_VERSION.equals(schemaVersion)) {
            event = (PaymentCancelledEvent) event.migrateFrom(schemaVersion);
        }

        return event;
    }

    @Override
    public PaymentCancelledEvent migrateFrom(String fromVersion) {
        if (fromVersion.startsWith("1.")) {
            return this;
        }
        throw new IllegalArgumentException("Unsupported schema version: " + fromVersion);
    }
}
