package com.payment.gateway.domain.transaction.exception;

/**
 * Exception thrown when transaction is not found.
 */
public class TransactionNotFoundException extends TransactionException {
    public TransactionNotFoundException(String transactionId) {
        super("Transaction not found with id: " + transactionId);
    }

    public TransactionNotFoundException(String transactionId, Throwable cause) {
        super("Transaction not found with id: " + transactionId, cause);
    }
}
