package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import com.payment.gateway.domain.reconciliation.model.ReconciliationBatch;
import com.payment.gateway.domain.reconciliation.model.ReconciliationStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ReconciliationMapper {

    public ReconciliationBatchJpaEntity toEntity(ReconciliationBatch batch) {
        return ReconciliationBatchJpaEntity.builder()
                .id(batch.getId())
                .merchantId(batch.getMerchantId())
                .reconciliationDate(batch.getReconciliationDate())
                .gatewayName(batch.getGatewayName())
                .status(batch.getStatus() != null ? batch.getStatus().name() : null)
                .totalTransactions(batch.getTotalTransactions())
                .matchedTransactions(batch.getMatchedTransactions())
                .unmatchedTransactions(batch.getUnmatchedTransactions())
                .totalAmount(batch.getTotalAmount())
                .matchedAmount(batch.getMatchedAmount())
                .discrepancyCount(batch.getDiscrepancyCount())
                .initiatedBy(batch.getInitiatedBy())
                .startedAt(batch.getStartedAt())
                .completedAt(batch.getCompletedAt())
                .createdAt(batch.getCreatedAt())
                .updatedAt(batch.getUpdatedAt())
                .build();
    }

    public ReconciliationBatch toDomain(ReconciliationBatchJpaEntity entity) {
        return ReconciliationBatch.builder()
                .id(entity.getId())
                .merchantId(entity.getMerchantId())
                .reconciliationDate(entity.getReconciliationDate())
                .gatewayName(entity.getGatewayName())
                .status(entity.getStatus() != null ? ReconciliationStatus.valueOf(entity.getStatus()) : null)
                .totalTransactions(entity.getTotalTransactions())
                .matchedTransactions(entity.getMatchedTransactions())
                .unmatchedTransactions(entity.getUnmatchedTransactions())
                .totalAmount(entity.getTotalAmount() != null ? entity.getTotalAmount() : BigDecimal.ZERO)
                .matchedAmount(entity.getMatchedAmount() != null ? entity.getMatchedAmount() : BigDecimal.ZERO)
                .unmatchedAmount(BigDecimal.ZERO)
                .discrepancyCount(entity.getDiscrepancyCount())
                .initiatedBy(entity.getInitiatedBy())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
