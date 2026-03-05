package com.payment.gateway.domain.payment.model;

import java.util.Arrays;

/**
 * Enum representing the payment method type.
 */
public enum PaymentMethod {

    CREDIT_CARD,
    DEBIT_CARD,
    PIX,
    BOLETO,
    BANK_TRANSFER,
    WALLET,
    CRYPTO;

    public static PaymentMethod fromString(String value) {
        return Arrays.stream(values())
                .filter(m -> m.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "Invalid payment method: " + value));
    }
}
