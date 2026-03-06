package com.payment.gateway.infrastructure.transaction.adapter.out.persistence;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.transaction.model.Transaction;
import com.payment.gateway.domain.transaction.model.TransactionStatus;
import com.payment.gateway.domain.transaction.model.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;

@Component
public class TransactionMapper {

    public TransactionJpaEntity toEntity(Transaction transaction) {
        return TransactionJpaEntity.builder()
                .id(transaction.getId())
                .paymentId(transaction.getPaymentId())
                .merchantId(transaction.getMerchantId())
                .type(transaction.getType() != null ? transaction.getType().name() : null)
                .amount(transaction.getAmount() != null ? transaction.getAmount().getAmount() : BigDecimal.ZERO)
                .netAmount(transaction.getNetAmount() != null ? transaction.getNetAmount().getAmount() : null)
                .currency(transaction.getCurrency())
                .status(transaction.getStatus() != null ? transaction.getStatus().name() : null)
                .gatewayTransactionId(transaction.getGatewayTransactionId())
                .errorCode(transaction.getErrorCode())
                .errorMessage(transaction.getErrorMessage())
                .retryCount(transaction.getRetryCount())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .processedAt(transaction.getProcessedAt())
                .build();
    }

    public Transaction toDomain(TransactionJpaEntity entity) {
        Currency currency = Currency.getInstance(entity.getCurrency());
        Money amount = entity.getAmount() != null
                ? Money.of(entity.getAmount(), currency)
                : Money.zero(currency);
        Money netAmount = entity.getNetAmount() != null
                ? Money.of(entity.getNetAmount(), currency)
                : null;

        return Transaction.builder()
                .id(entity.getId())
                .paymentId(entity.getPaymentId())
                .merchantId(entity.getMerchantId())
                .type(entity.getType() != null ? TransactionType.valueOf(entity.getType()) : null)
                .amount(amount)
                .netAmount(netAmount)
                .currency(entity.getCurrency())
                .status(entity.getStatus() != null ? TransactionStatus.valueOf(entity.getStatus()) : null)
                .gatewayTransactionId(entity.getGatewayTransactionId())
                .errorCode(entity.getErrorCode())
                .errorMessage(entity.getErrorMessage())
                .retryCount(entity.getRetryCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .processedAt(entity.getProcessedAt())
                .build();
    }
}
