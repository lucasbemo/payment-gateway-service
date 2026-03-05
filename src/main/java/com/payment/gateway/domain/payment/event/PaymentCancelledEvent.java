package com.payment.gateway.domain.payment.event;

import com.payment.gateway.commons.event.DomainEvent;
import lombok.Getter;

/**
 * Event published when a payment is cancelled.
 */
@Getter
public class PaymentCancelledEvent extends DomainEvent {

    private final String merchantId;
    private final String reason;

    public PaymentCancelledEvent(String aggregateId, String merchantId, String reason) {
        super(aggregateId, "Payment");
        this.merchantId = merchantId;
        this.reason = reason;
    }

    @Override
    public String getEventType() {
        return "PAYMENT_CANCELLED";
    }
}
