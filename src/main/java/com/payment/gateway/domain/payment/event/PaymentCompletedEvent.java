package com.payment.gateway.domain.payment.event;

import com.payment.gateway.commons.event.IntegrationEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event published when a payment is completed.
 * Supports schema versioning for backward compatibility.
 */
@Getter
public class PaymentCompletedEvent extends IntegrationEvent {

    public static final String CURRENT_SCHEMA_VERSION = "1.0.0";

    private final String merchantId;
    private final String amount;
    private final String currency;
    private final String providerTransactionId;

    public PaymentCompletedEvent(String aggregateId, String merchantId, String amount,
                                  String currency, String providerTransactionId) {
        super(aggregateId, CURRENT_SCHEMA_VERSION, "PAYMENT_COMPLETED");
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.providerTransactionId = providerTransactionId;
    }

    public PaymentCompletedEvent(String id, Instant occurredOn, String aggregateId,
                                  String merchantId, String amount, String currency,
                                  String providerTransactionId, String schemaVersion) {
        super(id, occurredOn, aggregateId, schemaVersion, "PAYMENT_COMPLETED");
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.providerTransactionId = providerTransactionId;
    }

    @Override
    protected void populateMap(Map<String, Object> map) {
        map.put("merchantId", merchantId);
        map.put("amount", amount);
        map.put("currency", currency);
        map.put("providerTransactionId", providerTransactionId);
    }

    @SuppressWarnings("unchecked")
    public static PaymentCompletedEvent fromMap(Map<String, Object> map) {
        String schemaVersion = (String) map.get("schemaVersion");
        if (schemaVersion == null) {
            schemaVersion = CURRENT_SCHEMA_VERSION;
        }

        PaymentCompletedEvent event = new PaymentCompletedEvent(
            (String) map.get("id"),
            (Instant) map.get("occurredOn"),
            (String) map.get("aggregateId"),
            (String) map.get("merchantId"),
            (String) map.get("amount"),
            (String) map.get("currency"),
            (String) map.get("providerTransactionId"),
            schemaVersion
        );

        if (!CURRENT_SCHEMA_VERSION.equals(schemaVersion)) {
            event = (PaymentCompletedEvent) event.migrateFrom(schemaVersion);
        }

        return event;
    }

    @Override
    public PaymentCompletedEvent migrateFrom(String fromVersion) {
        if (fromVersion.startsWith("1.")) {
            return this;
        }
        throw new IllegalArgumentException("Unsupported schema version: " + fromVersion);
    }
}
