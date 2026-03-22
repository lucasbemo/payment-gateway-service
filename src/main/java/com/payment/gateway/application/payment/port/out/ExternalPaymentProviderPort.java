package com.payment.gateway.application.payment.port.out;

import java.util.concurrent.CompletableFuture;

public interface ExternalPaymentProviderPort {

    CompletableFuture<PaymentProviderResult> authorize(PaymentProviderRequest request);

    CompletableFuture<PaymentProviderResult> capture(PaymentProviderRequest request);

    CompletableFuture<PaymentProviderResult> cancel(PaymentProviderRequest request);

    CompletableFuture<String> tokenizeCard(CardTokenizationRequest request);

    String getProviderName();

    boolean isHealthy();

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
