package com.payment.gateway.application.refund.port.out;

import java.util.concurrent.CompletableFuture;

public interface ExternalRefundProviderPort {

    CompletableFuture<RefundProviderResult> processRefund(RefundProviderRequest request);

    CompletableFuture<RefundStatusResult> getRefundStatus(String providerRefundId);

    String getProviderName();

    boolean isHealthy();

    record RefundProviderRequest(
        String refundId,
        String originalPaymentId,
        String providerTransactionId,
        Long amount,
        String currency,
        String reason
    ) {}

    record RefundProviderResult(
        boolean success,
        String providerRefundId,
        String errorCode,
        String errorMessage
    ) {}

    record RefundStatusResult(
        String status,
        java.time.Instant processedAt
    ) {}
}