package com.payment.gateway.commons.exception;

/**
 * Exception thrown when a business rule is violated.
 */
public class BusinessException extends DomainException {

    public BusinessException(String message) {
        super("BUSINESS_ERROR", message);
    }

    public BusinessException(String code, String message) {
        super(code, message);
    }
}
