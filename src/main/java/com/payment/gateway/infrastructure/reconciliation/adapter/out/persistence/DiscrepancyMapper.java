package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.reconciliation.model.Discrepancy;
import com.payment.gateway.domain.reconciliation.model.DiscrepancyStatus;
import com.payment.gateway.domain.reconciliation.model.DiscrepancyType;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class DiscrepancyMapper {

    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("USD");

    public DiscrepancyJpaEntity toEntity(Discrepancy domain) {
        return DiscrepancyJpaEntity.builder()
                .id(domain.getId())
                .reconciliationBatchId(domain.getBatchId())
                .transactionId(domain.getTransactionId())
                .paymentId(domain.getMerchantId() != null ? domain.getMerchantId() : "")
                .discrepancyType(domain.getType() != null ? domain.getType().name() : null)
                .description(domain.getDescription())
                .expectedAmount(domain.getSystemAmount() != null ? domain.getSystemAmount().getAmount() : null)
                .actualAmount(domain.getGatewayAmount() != null ? domain.getGatewayAmount().getAmount() : null)
                .resolutionStatus(domain.getStatus() != null ? domain.getStatus().name() : DiscrepancyStatus.OPEN.name())
                .resolutionNotes(domain.getResolutionNotes())
                .resolvedBy(domain.getResolvedBy())
                .resolvedAt(domain.getResolvedAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public Discrepancy toDomain(DiscrepancyJpaEntity entity) {
        Money systemAmount = entity.getExpectedAmount() != null
                ? Money.of(entity.getExpectedAmount(), DEFAULT_CURRENCY)
                : null;
        Money gatewayAmount = entity.getActualAmount() != null
                ? Money.of(entity.getActualAmount(), DEFAULT_CURRENCY)
                : null;

        return Discrepancy.builder()
                .id(entity.getId())
                .batchId(entity.getReconciliationBatchId())
                .merchantId(entity.getPaymentId())
                .transactionId(entity.getTransactionId())
                .type(entity.getDiscrepancyType() != null ? DiscrepancyType.valueOf(entity.getDiscrepancyType()) : null)
                .status(entity.getResolutionStatus() != null ? DiscrepancyStatus.valueOf(entity.getResolutionStatus()) : null)
                .systemAmount(systemAmount)
                .gatewayAmount(gatewayAmount)
                .description(entity.getDescription())
                .resolutionNotes(entity.getResolutionNotes())
                .resolvedBy(entity.getResolvedBy())
                .resolvedAt(entity.getResolvedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
