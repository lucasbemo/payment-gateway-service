package com.payment.gateway.application.reconciliation.service;

import com.payment.gateway.application.reconciliation.dto.ReconciliationResponse;
import com.payment.gateway.application.reconciliation.port.in.ReconcileTransactionsUseCase;
import com.payment.gateway.application.reconciliation.port.out.ReconciliationBatchPort;
import com.payment.gateway.domain.reconciliation.model.ReconciliationBatch;
import com.payment.gateway.domain.reconciliation.service.ReconciliationDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Application service for reconciling transactions.
 */
@Slf4j
@Service
@Transactional
public class ReconcileTransactionsService implements ReconcileTransactionsUseCase {

    private final ReconciliationBatchPort reconciliationBatchPort;
    private final ReconciliationDomainService reconciliationDomainService;

    public ReconcileTransactionsService(ReconciliationBatchPort reconciliationBatchPort,
                                         ReconciliationDomainService reconciliationDomainService) {
        this.reconciliationBatchPort = reconciliationBatchPort;
        this.reconciliationDomainService = reconciliationDomainService;
    }

    @Override
    public ReconciliationResponse reconcileTransactions(String merchantId, String date) {
        log.info("Starting reconciliation for merchant: {} on date: {}", merchantId, date);

        LocalDate reconciliationDate = LocalDate.parse(date);

        // Create and start reconciliation batch
        ReconciliationBatch batch = reconciliationDomainService.createReconciliationBatch(
                merchantId, reconciliationDate, "DEFAULT_GATEWAY", "system");

        // Start processing
        batch = reconciliationDomainService.startReconciliation(batch.getId());

        // Complete reconciliation (in a real implementation, this would involve actual reconciliation logic)
        batch = reconciliationDomainService.completeReconciliation(batch.getId());

        log.info("Reconciliation completed for batch: {}", batch.getId());

        return mapToResponse(batch);
    }

    private ReconciliationResponse mapToResponse(ReconciliationBatch batch) {
        return ReconciliationResponse.builder()
                .batchId(batch.getId())
                .status(batch.getStatus().name())
                .totalTransactions(batch.getTotalTransactions())
                .matchedCount(batch.getMatchedTransactions())
                .discrepancyCount(batch.getDiscrepancyCount())
                .totalAmount(batch.getTotalAmount() != null ? batch.getTotalAmount().longValueExact() : 0L)
                .reconciledAmount(batch.getMatchedAmount() != null ? batch.getMatchedAmount().longValueExact() : 0L)
                .discrepancyAmount(batch.getUnmatchedAmount() != null ? batch.getUnmatchedAmount().longValueExact() : 0L)
                .createdAt(batch.getCreatedAt())
                .completedAt(batch.getCompletedAt())
                .build();
    }
}
