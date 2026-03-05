package com.payment.gateway.application.payment.port.out;

/**
 * Output port for tokenization operations.
 */
public interface TokenizationServicePort {

    /**
     * Tokenize sensitive card data.
     */
    String tokenize(String cardNumber, String expiryMonth, String expiryYear, String cvv);

    /**
     * Detokenize a token to retrieve card data.
     */
    CardData detokenize(String token);

    record CardData(
        String cardNumber,
        String expiryMonth,
        String expiryYear,
        String cvv
    ) {}
}
