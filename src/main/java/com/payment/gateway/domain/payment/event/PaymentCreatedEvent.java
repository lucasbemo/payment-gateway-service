package com.payment.gateway.domain.payment.event;

import com.payment.gateway.commons.event.DomainEvent;
import lombok.Getter;

import java.time.Instant;

/**
 * Event published when a payment is created.
 */
@Getter
public class PaymentCreatedEvent extends DomainEvent {

    private final String merchantId;
    private final String amount;
    private final String currency;
    private final String idempotencyKey;

    public PaymentCreatedEvent(String aggregateId, String merchantId, String amount,
                               String currency, String idempotencyKey) {
        super(aggregateId, "Payment");
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.idempotencyKey = idempotencyKey;
    }

    @Override
    public String getEventType() {
        return "PAYMENT_CREATED";
    }
}
