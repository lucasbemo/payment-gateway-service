package com.payment.gateway.application.transaction.port.out;

/**
 * Output port for external transaction provider operations.
 */
public interface ExternalTransactionProviderPort {

    CaptureResult capture(CaptureRequest request);

    record CaptureRequest(
            String transactionId,
            String gatewayTransactionId,
            Long amount,
            String currency
    ) {}

    record CaptureResult(
            boolean success,
            String gatewayTransactionId,
            String errorCode,
            String errorMessage
    ) {}
}
