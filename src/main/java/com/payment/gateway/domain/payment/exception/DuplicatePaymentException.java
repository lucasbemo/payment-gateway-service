package com.payment.gateway.domain.payment.exception;

import com.payment.gateway.commons.exception.BusinessException;

/**
 * Exception thrown when a duplicate payment is detected.
 */
public class DuplicatePaymentException extends BusinessException {

    public DuplicatePaymentException(String idempotencyKey) {
        super("DUPLICATE_PAYMENT", "A payment with idempotency key already exists: " + idempotencyKey);
    }
}
