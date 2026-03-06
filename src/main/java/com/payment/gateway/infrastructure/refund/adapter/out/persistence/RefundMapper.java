package com.payment.gateway.infrastructure.refund.adapter.out.persistence;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.refund.model.Refund;
import com.payment.gateway.domain.refund.model.RefundStatus;
import com.payment.gateway.domain.refund.model.RefundType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Currency;

@Component
public class RefundMapper {

    public RefundJpaEntity toEntity(Refund refund) {
        return RefundJpaEntity.builder()
                .id(refund.getId())
                .paymentId(refund.getPaymentId())
                .transactionId(refund.getTransactionId())
                .merchantId(refund.getMerchantId())
                .refundIdempotencyKey(refund.getRefundIdempotencyKey())
                .type(refund.getType() != null ? refund.getType().name() : null)
                .amount(refund.getAmount() != null ? refund.getAmount().getAmount() : null)
                .refundedAmount(refund.getRefundedAmount() != null ? refund.getRefundedAmount().getAmount() : null)
                .currency(refund.getCurrency())
                .status(refund.getStatus() != null ? refund.getStatus().name() : null)
                .reason(refund.getReason())
                .gatewayRefundId(refund.getGatewayRefundId())
                .errorCode(refund.getErrorCode())
                .errorMessage(refund.getErrorMessage())
                .retryCount(refund.getRetryCount())
                .createdAt(refund.getCreatedAt())
                .updatedAt(refund.getUpdatedAt())
                .processedAt(refund.getProcessedAt())
                .build();
    }

    public Refund toDomain(RefundJpaEntity entity) {
        Currency currency = Currency.getInstance(entity.getCurrency());
        Money amount = entity.getAmount() != null
                ? Money.of(entity.getAmount(), currency)
                : Money.zero(currency);
        Money refundedAmount = entity.getRefundedAmount() != null
                ? Money.of(entity.getRefundedAmount(), currency)
                : Money.zero(currency);

        return Refund.builder()
                .id(entity.getId())
                .paymentId(entity.getPaymentId())
                .transactionId(entity.getTransactionId())
                .merchantId(entity.getMerchantId())
                .refundIdempotencyKey(entity.getRefundIdempotencyKey())
                .type(entity.getType() != null ? RefundType.valueOf(entity.getType()) : null)
                .amount(amount)
                .refundedAmount(refundedAmount)
                .currency(entity.getCurrency())
                .status(entity.getStatus() != null ? RefundStatus.valueOf(entity.getStatus()) : null)
                .reason(entity.getReason())
                .gatewayRefundId(entity.getGatewayRefundId())
                .errorCode(entity.getErrorCode())
                .errorMessage(entity.getErrorMessage())
                .items(new ArrayList<>())
                .retryCount(entity.getRetryCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .processedAt(entity.getProcessedAt())
                .build();
    }
}
