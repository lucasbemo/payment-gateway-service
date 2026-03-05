package com.payment.gateway.domain.payment.exception;

import com.payment.gateway.commons.exception.DomainException;

/**
 * Exception thrown when a payment is not found.
 */
public class PaymentNotFoundException extends DomainException {

    public PaymentNotFoundException(String paymentId) {
        super("PAYMENT_NOT_FOUND", "Payment not found with id: " + paymentId);
    }
}
