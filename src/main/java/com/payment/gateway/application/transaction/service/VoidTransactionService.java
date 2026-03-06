package com.payment.gateway.application.transaction.service;

import com.payment.gateway.application.transaction.dto.TransactionResponse;
import com.payment.gateway.application.transaction.port.in.VoidTransactionUseCase;
import com.payment.gateway.application.transaction.port.out.TransactionCommandPort;
import com.payment.gateway.application.transaction.port.out.TransactionQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.transaction.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for voiding transactions.
 */
@Slf4j
@Service
@Transactional
public class VoidTransactionService implements VoidTransactionUseCase {

    private final TransactionQueryPort transactionQueryPort;
    private final TransactionCommandPort transactionCommandPort;

    public VoidTransactionService(TransactionQueryPort transactionQueryPort, TransactionCommandPort transactionCommandPort) {
        this.transactionQueryPort = transactionQueryPort;
        this.transactionCommandPort = transactionCommandPort;
    }

    @Override
    public TransactionResponse voidTransaction(String transactionId, String merchantId) {
        log.info("Voiding transaction: {} for merchant: {}", transactionId, merchantId);

        Transaction transaction = transactionQueryPort.findByIdAndMerchantId(transactionId, merchantId)
                .orElseThrow(() -> new BusinessException("Transaction not found: " + transactionId));

        // Validate transaction can be voided
        if (!transaction.getStatus().canTransitionTo(com.payment.gateway.domain.transaction.model.TransactionStatus.REVERSED)) {
            throw new BusinessException("Cannot void transaction in current state: " + transaction.getStatus());
        }

        // Void the transaction
        transaction.reverse();
        Transaction voidedTransaction = transactionCommandPort.updateTransaction(transaction);

        log.info("Transaction voided successfully: {}", transactionId);
        return mapToResponse(voidedTransaction);
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
