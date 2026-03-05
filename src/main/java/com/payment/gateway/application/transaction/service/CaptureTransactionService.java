package com.payment.gateway.application.transaction.service;

import com.payment.gateway.application.transaction.dto.TransactionResponse;
import com.payment.gateway.application.transaction.port.in.CaptureTransactionUseCase;
import com.payment.gateway.application.transaction.port.out.ExternalTransactionProviderPort;
import com.payment.gateway.application.transaction.port.out.TransactionCommandPort;
import com.payment.gateway.application.transaction.port.out.TransactionQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.transaction.model.Transaction;
import lombok.extern.slf4j.Slf4j;

/**
 * Application service for capturing transactions.
 */
@Slf4j
public class CaptureTransactionService implements CaptureTransactionUseCase {

    private final TransactionQueryPort transactionQueryPort;
    private final TransactionCommandPort transactionCommandPort;
    private final ExternalTransactionProviderPort externalTransactionProviderPort;

    public CaptureTransactionService(TransactionQueryPort transactionQueryPort,
                                     TransactionCommandPort transactionCommandPort,
                                     ExternalTransactionProviderPort externalTransactionProviderPort) {
        this.transactionQueryPort = transactionQueryPort;
        this.transactionCommandPort = transactionCommandPort;
        this.externalTransactionProviderPort = externalTransactionProviderPort;
    }

    @Override
    public TransactionResponse captureTransaction(String transactionId, String merchantId) {
        log.info("Capturing transaction: {} for merchant: {}", transactionId, merchantId);

        Transaction transaction = transactionQueryPort.findByIdAndMerchantId(transactionId, merchantId)
                .orElseThrow(() -> new BusinessException("Transaction not found: " + transactionId));

        // Validate transaction can be captured
        if (!transaction.getStatus().canTransitionTo(com.payment.gateway.domain.transaction.model.TransactionStatus.CAPTURED)) {
            throw new BusinessException("Cannot capture transaction in current state: " + transaction.getStatus());
        }

        // Capture with external provider
        captureWithProvider(transaction);

        // Update transaction status
        transaction.capture();
        Transaction capturedTransaction = transactionCommandPort.updateTransaction(transaction);

        log.info("Transaction captured successfully: {}", transactionId);
        return mapToResponse(capturedTransaction);
    }

    private void captureWithProvider(Transaction transaction) {
        log.debug("Capturing transaction {} with external provider", transaction.getId());

        ExternalTransactionProviderPort.CaptureRequest request =
                new ExternalTransactionProviderPort.CaptureRequest(
                        transaction.getId(),
                        transaction.getGatewayTransactionId(),
                        transaction.getAmount().getAmountInCents(),
                        transaction.getCurrency()
                );

        ExternalTransactionProviderPort.CaptureResult result =
                externalTransactionProviderPort.capture(request);

        if (!result.success()) {
            log.warn("Transaction capture with provider failed: {} - {}",
                    result.errorCode(), result.errorMessage());
            throw new BusinessException("Transaction capture failed: " + result.errorMessage());
        }

        // Update gateway transaction ID if provided
        if (result.gatewayTransactionId() != null) {
            transaction.updateGatewayTransactionId(result.gatewayTransactionId());
        }
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .paymentId(transaction.getPaymentId())
                .merchantId(transaction.getMerchantId())
                .type(transaction.getType().name())
                .amount(transaction.getAmount().getAmountInCents())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus().name())
                .gatewayTransactionId(transaction.getGatewayTransactionId())
                .errorCode(transaction.getErrorCode())
                .errorMessage(transaction.getErrorMessage())
                .createdAt(transaction.getCreatedAt())
                .processedAt(transaction.getProcessedAt())
                .build();
    }
}
