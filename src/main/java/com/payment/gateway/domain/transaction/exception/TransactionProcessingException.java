package com.payment.gateway.domain.transaction.exception;

/**
 * Exception thrown when transaction processing fails.
 */
public class TransactionProcessingException extends TransactionException {
    public TransactionProcessingException(String message) {
        super(message);
    }

    public TransactionProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
