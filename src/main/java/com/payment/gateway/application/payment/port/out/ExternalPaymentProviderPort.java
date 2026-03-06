package com.payment.gateway.application.payment.port.out;

import java.util.concurrent.CompletableFuture;

/**
 * Output port for external payment provider operations.
 */
public interface ExternalPaymentProviderPort {

    /**
     * Authorize a payment with the external provider.
     */
    CompletableFuture<PaymentProviderResult> authorize(PaymentProviderRequest request);

    /**
     * Capture a payment with the external provider.
     */
    CompletableFuture<PaymentProviderResult> capture(PaymentProviderRequest request);

    /**
     * Cancel a payment with the external provider.
     */
    CompletableFuture<PaymentProviderResult> cancel(PaymentProviderRequest request);

    /**
     * Tokenize card information.
     */
    CompletableFuture<String> tokenizeCard(CardTokenizationRequest request);

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
