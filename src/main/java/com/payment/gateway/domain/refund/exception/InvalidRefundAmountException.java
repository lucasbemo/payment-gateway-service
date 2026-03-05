package com.payment.gateway.domain.refund.exception;

/**
 * Exception thrown when refund amount exceeds original transaction amount.
 */
public class InvalidRefundAmountException extends RefundException {
    public InvalidRefundAmountException(String message) {
        super(message);
    }
}
