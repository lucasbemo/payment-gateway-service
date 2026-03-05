package com.payment.gateway.domain.payment.port;

/**
 * Port for publishing payment events.
 */
public interface PaymentEventPublisherPort {

    void publishPaymentCreated(String paymentId, String merchantId, String amount, String currency, String idempotencyKey);

    void publishPaymentCompleted(String paymentId, String merchantId, String amount, String currency, String providerTransactionId);

    void publishPaymentFailed(String paymentId, String merchantId, String amount, String currency, String errorCode, String errorMessage);

    void publishPaymentCancelled(String paymentId, String merchantId, String reason);

    void publishRefundProcessed(String refundId, String paymentId, String merchantId, String refundAmount, String currency, String refundType);
}
