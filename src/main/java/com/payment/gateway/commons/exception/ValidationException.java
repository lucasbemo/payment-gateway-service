package com.payment.gateway.commons.exception;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends DomainException {

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }

    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR", "Field '" + field + "': " + message);
    }
}
