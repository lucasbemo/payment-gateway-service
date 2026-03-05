package com.payment.gateway.domain.refund.exception;

/**
 * Exception thrown when refund is not found.
 */
public class RefundNotFoundException extends RefundException {
    public RefundNotFoundException(String refundId) {
        super("Refund not found with id: " + refundId);
    }

    public RefundNotFoundException(String refundId, Throwable cause) {
        super("Refund not found with id: " + refundId, cause);
    }
}
