package com.payment.gateway.domain.payment.event;

import com.payment.gateway.commons.event.DomainEvent;
import lombok.Getter;

/**
 * Event published when a refund is processed.
 */
@Getter
public class RefundProcessedEvent extends DomainEvent {

    private final String paymentId;
    private final String merchantId;
    private final String refundAmount;
    private final String currency;
    private final String refundType;

    public RefundProcessedEvent(String aggregateId, String paymentId, String merchantId,
                                 String refundAmount, String currency, String refundType) {
        super(aggregateId, "Refund");
        this.paymentId = paymentId;
        this.merchantId = merchantId;
        this.refundAmount = refundAmount;
        this.currency = currency;
        this.refundType = refundType;
    }

    @Override
    public String getEventType() {
        return "REFUND_PROCESSED";
    }
}
