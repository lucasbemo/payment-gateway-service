package com.payment.gateway.domain.transaction.exception;

/**
 * Exception thrown when transaction state transition is invalid.
 */
public class InvalidTransactionStateException extends TransactionException {
    public InvalidTransactionStateException(String currentState, String newState) {
        super("Invalid state transition from " + currentState + " to " + newState);
    }
}
