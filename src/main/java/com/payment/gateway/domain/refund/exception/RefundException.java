package com.payment.gateway.domain.refund.exception;

import com.payment.gateway.commons.exception.DomainException;

/**
 * Base exception for refund domain.
 */
public class RefundException extends DomainException {
    public RefundException(String message) {
        super(message);
    }

    public RefundException(String message, Throwable cause) {
        super(message, cause);
    }
}
