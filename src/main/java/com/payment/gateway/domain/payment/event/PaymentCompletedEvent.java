package com.payment.gateway.domain.payment.event;

import com.payment.gateway.commons.event.DomainEvent;
import lombok.Getter;

/**
 * Event published when a payment is completed.
 */
@Getter
public class PaymentCompletedEvent extends DomainEvent {

    private final String merchantId;
    private final String amount;
    private final String currency;
    private final String providerTransactionId;

    public PaymentCompletedEvent(String aggregateId, String merchantId, String amount,
                                  String currency, String providerTransactionId) {
        super(aggregateId, "Payment");
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.providerTransactionId = providerTransactionId;
    }

    @Override
    public String getEventType() {
        return "PAYMENT_COMPLETED";
    }
}
