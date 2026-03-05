package com.payment.gateway.application.customer.port.out;

/**
 * Output port for tokenization operations.
 */
public interface TokenizationServicePort {

    String tokenize(String cardNumber, String expiryMonth, String expiryYear, String cvv);
}
