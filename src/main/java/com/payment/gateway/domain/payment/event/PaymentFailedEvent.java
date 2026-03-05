package com.payment.gateway.domain.payment.event;

import com.payment.gateway.commons.event.DomainEvent;
import lombok.Getter;

/**
 * Event published when a payment fails.
 */
@Getter
public class PaymentFailedEvent extends DomainEvent {

    private final String merchantId;
    private final String amount;
    private final String currency;
    private final String errorCode;
    private final String errorMessage;

    public PaymentFailedEvent(String aggregateId, String merchantId, String amount,
                               String currency, String errorCode, String errorMessage) {
        super(aggregateId, "Payment");
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String getEventType() {
        return "PAYMENT_FAILED";
    }
}
