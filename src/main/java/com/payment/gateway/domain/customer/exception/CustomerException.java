package com.payment.gateway.domain.customer.exception;

import com.payment.gateway.commons.exception.DomainException;

/**
 * Base exception for customer domain.
 */
public class CustomerException extends DomainException {
    public CustomerException(String message) {
        super(message);
    }

    public CustomerException(String message, Throwable cause) {
        super(message, cause);
    }
}
