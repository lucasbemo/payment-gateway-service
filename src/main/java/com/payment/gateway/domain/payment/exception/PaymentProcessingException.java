package com.payment.gateway.domain.payment.exception;

import com.payment.gateway.commons.exception.DomainException;

/**
 * Exception thrown when payment processing fails.
 */
public class PaymentProcessingException extends DomainException {

    public PaymentProcessingException(String message) {
        super("PAYMENT_PROCESSING_ERROR", message);
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super("PAYMENT_PROCESSING_ERROR", message, cause);
    }
}
