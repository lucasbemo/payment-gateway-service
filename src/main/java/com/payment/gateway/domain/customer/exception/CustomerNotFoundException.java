package com.payment.gateway.domain.customer.exception;

/**
 * Exception thrown when customer is not found.
 */
public class CustomerNotFoundException extends CustomerException {
    public CustomerNotFoundException(String customerId) {
        super("Customer not found with id: " + customerId);
    }

    public CustomerNotFoundException(String customerId, Throwable cause) {
        super("Customer not found with id: " + customerId, cause);
    }
}
