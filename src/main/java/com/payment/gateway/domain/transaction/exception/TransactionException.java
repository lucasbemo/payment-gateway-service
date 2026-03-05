package com.payment.gateway.domain.transaction.exception;

import com.payment.gateway.commons.exception.DomainException;

/**
 * Base exception for transaction domain.
 */
public class TransactionException extends DomainException {
    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
