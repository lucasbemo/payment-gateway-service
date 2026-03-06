package com.payment.gateway.domain.payment.event;

import com.payment.gateway.commons.event.IntegrationEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event published when a payment is created.
 * Supports schema versioning for backward compatibility.
 */
@Getter
public class PaymentCreatedEvent extends IntegrationEvent {

    public static final String CURRENT_SCHEMA_VERSION = "1.0.0";

    private final String merchantId;
    private final String amount;
    private final String currency;
    private final String idempotencyKey;

    public PaymentCreatedEvent(String aggregateId, String merchantId, String amount,
                               String currency, String idempotencyKey) {
        super(aggregateId, CURRENT_SCHEMA_VERSION, "PAYMENT_CREATED");
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.idempotencyKey = idempotencyKey;
    }

    public PaymentCreatedEvent(String id, Instant occurredOn, String aggregateId,
                                String merchantId, String amount, String currency,
                                String idempotencyKey, String schemaVersion) {
        super(id, occurredOn, aggregateId, schemaVersion, "PAYMENT_CREATED");
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.idempotencyKey = idempotencyKey;
    }

    @Override
    protected void populateMap(Map<String, Object> map) {
        map.put("merchantId", merchantId);
        map.put("amount", amount);
        map.put("currency", currency);
        map.put("idempotencyKey", idempotencyKey);
    }

    @SuppressWarnings("unchecked")
    public static PaymentCreatedEvent fromMap(Map<String, Object> map) {
        String schemaVersion = (String) map.get("schemaVersion");
        if (schemaVersion == null) {
            schemaVersion = CURRENT_SCHEMA_VERSION;
        }

        PaymentCreatedEvent event = new PaymentCreatedEvent(
            (String) map.get("id"),
            (Instant) map.get("occurredOn"),
            (String) map.get("aggregateId"),
            (String) map.get("merchantId"),
            (String) map.get("amount"),
            (String) map.get("currency"),
            (String) map.get("idempotencyKey"),
            schemaVersion
        );

        // Migrate if needed
        if (!CURRENT_SCHEMA_VERSION.equals(schemaVersion)) {
            event = (PaymentCreatedEvent) event.migrateFrom(schemaVersion);
        }

        return event;
    }

    @Override
    public PaymentCreatedEvent migrateFrom(String fromVersion) {
        // Handle schema migrations here if needed in the future
        // For now, all 1.x.x versions are compatible
        if (fromVersion.startsWith("1.")) {
            return this;
        }
        throw new IllegalArgumentException("Unsupported schema version: " + fromVersion);
    }
}
