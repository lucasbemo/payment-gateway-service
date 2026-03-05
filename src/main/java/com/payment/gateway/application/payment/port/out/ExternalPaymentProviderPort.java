package com.payment.gateway.application.payment.port.out;

/**
 * Output port for external payment provider operations.
 */
public interface ExternalPaymentProviderPort {

    /**
     * Authorize a payment with the external provider.
     */
    PaymentProviderResult authorize(PaymentProviderRequest request);

    /**
     * Capture a payment with the external provider.
     */
    PaymentProviderResult capture(PaymentProviderRequest request);

    /**
     * Cancel a payment with the external provider.
     */
    PaymentProviderResult cancel(PaymentProviderRequest request);

    /**
     * Tokenize card information.
     */
    String tokenizeCard(CardTokenizationRequest request);

    record PaymentProviderRequest(
        String paymentId,
        String merchantId,
        Long amount,
        String currency,
        String paymentMethodToken
    ) {}

    record PaymentProviderResult(
        boolean success,
        String providerTransactionId,
        String errorCode,
        String errorMessage
    ) {}

    record CardTokenizationRequest(
        String cardNumber,
        String expiryMonth,
        String expiryYear,
        String cvv
    ) {}
}
