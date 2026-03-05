package com.payment.gateway.domain.customer.exception;

/**
 * Exception thrown when payment method is invalid or not found.
 */
public class InvalidPaymentMethodException extends CustomerException {
    public InvalidPaymentMethodException(String message) {
        super(message);
    }

    public InvalidPaymentMethodException(String paymentMethodId, String customerId) {
        super("Payment method " + paymentMethodId + " not found for customer " + customerId);
    }
}
