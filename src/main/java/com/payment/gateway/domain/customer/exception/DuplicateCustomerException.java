package com.payment.gateway.domain.customer.exception;

/**
 * Exception thrown when customer already exists (duplicate email).
 */
public class DuplicateCustomerException extends CustomerException {
    public DuplicateCustomerException(String email) {
        super("Customer already exists with email: " + email);
    }
}
