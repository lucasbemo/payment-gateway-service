package com.payment.gateway.domain.payment.event;

import com.payment.gateway.commons.event.IntegrationEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event published when a payment fails.
 * Supports schema versioning for backward compatibility.
 */
@Getter
public class PaymentFailedEvent extends IntegrationEvent {

    public static final String CURRENT_SCHEMA_VERSION = "1.0.0";

    private final String merchantId;
    private final String amount;
    private final String currency;
    private final String errorCode;
    private final String errorMessage;

    public PaymentFailedEvent(String aggregateId, String merchantId, String amount,
                               String currency, String errorCode, String errorMessage) {
        super(aggregateId, CURRENT_SCHEMA_VERSION, "PAYMENT_FAILED");
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public PaymentFailedEvent(String id, Instant occurredOn, String aggregateId,
                               String merchantId, String amount, String currency,
                               String errorCode, String errorMessage, String schemaVersion) {
        super(id, occurredOn, aggregateId, schemaVersion, "PAYMENT_FAILED");
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    protected void populateMap(Map<String, Object> map) {
        map.put("merchantId", merchantId);
        map.put("amount", amount);
        map.put("currency", currency);
        map.put("errorCode", errorCode);
        map.put("errorMessage", errorMessage);
    }

    @SuppressWarnings("unchecked")
    public static PaymentFailedEvent fromMap(Map<String, Object> map) {
        String schemaVersion = (String) map.get("schemaVersion");
        if (schemaVersion == null) {
            schemaVersion = CURRENT_SCHEMA_VERSION;
        }

        PaymentFailedEvent event = new PaymentFailedEvent(
            (String) map.get("id"),
            (Instant) map.get("occurredOn"),
            (String) map.get("aggregateId"),
            (String) map.get("merchantId"),
            (String) map.get("amount"),
            (String) map.get("currency"),
            (String) map.get("errorCode"),
            (String) map.get("errorMessage"),
            schemaVersion
        );

        if (!CURRENT_SCHEMA_VERSION.equals(schemaVersion)) {
            event = (PaymentFailedEvent) event.migrateFrom(schemaVersion);
        }

        return event;
    }

    @Override
    public PaymentFailedEvent migrateFrom(String fromVersion) {
        if (fromVersion.startsWith("1.")) {
            return this;
        }
        throw new IllegalArgumentException("Unsupported schema version: " + fromVersion);
    }
}
