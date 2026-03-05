package com.payment.gateway.application.transaction.service;

import com.payment.gateway.application.transaction.dto.TransactionResponse;
import com.payment.gateway.application.transaction.port.in.GetTransactionUseCase;
import com.payment.gateway.application.transaction.port.out.TransactionQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.transaction.model.Transaction;
import lombok.extern.slf4j.Slf4j;

/**
 * Application service for getting transaction information.
 */
@Slf4j
public class GetTransactionService implements GetTransactionUseCase {

    private final TransactionQueryPort transactionQueryPort;

    public GetTransactionService(TransactionQueryPort transactionQueryPort) {
        this.transactionQueryPort = transactionQueryPort;
    }

    @Override
    public TransactionResponse getTransactionById(String transactionId, String merchantId) {
        log.info("Getting transaction by id: {} for merchant: {}", transactionId, merchantId);

        Transaction transaction = transactionQueryPort.findByIdAndMerchantId(transactionId, merchantId)
                .orElseThrow(() -> new BusinessException("Transaction not found: " + transactionId));

        return mapToResponse(transaction);
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
