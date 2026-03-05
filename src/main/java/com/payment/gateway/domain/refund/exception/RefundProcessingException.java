package com.payment.gateway.domain.refund.exception;

/**
 * Exception thrown when refund processing fails.
 */
public class RefundProcessingException extends RefundException {
    public RefundProcessingException(String message) {
        super(message);
    }

    public RefundProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
