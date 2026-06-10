package com.payment.gateway.application.reconciliation.service;

import com.payment.gateway.application.reconciliation.dto.ReconciliationResponse;
import com.payment.gateway.application.reconciliation.port.in.ReconcileTransactionsUseCase;
import com.payment.gateway.application.reconciliation.port.out.ReconciliationBatchPort;
import com.payment.gateway.application.transaction.port.out.TransactionQueryPort;
import com.payment.gateway.domain.reconciliation.model.ReconciliationBatch;
import com.payment.gateway.domain.reconciliation.service.ReconciliationDomainService;
import com.payment.gateway.domain.transaction.model.Transaction;
import com.payment.gateway.domain.transaction.model.TransactionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

/**
 * Application service for reconciling transactions.
 */
@Slf4j
@Service
@Transactional
public class ReconcileTransactionsService implements ReconcileTransactionsUseCase {

    private static final Set<TransactionStatus> MATCHED_STATUSES =
            Set.of(TransactionStatus.CAPTURED, TransactionStatus.SETTLED);
    private static final Set<TransactionStatus> DISCREPANCY_STATUSES =
            Set.of(TransactionStatus.FAILED, TransactionStatus.PENDING);

    private final ReconciliationBatchPort reconciliationBatchPort;
    private final ReconciliationDomainService reconciliationDomainService;
    private final TransactionQueryPort transactionQueryPort;

    public ReconcileTransactionsService(ReconciliationBatchPort reconciliationBatchPort,
                                         ReconciliationDomainService reconciliationDomainService,
                                         TransactionQueryPort transactionQueryPort) {
        this.reconciliationBatchPort = reconciliationBatchPort;
        this.reconciliationDomainService = reconciliationDomainService;
        this.transactionQueryPort = transactionQueryPort;
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

        // Load the merchant's transactions for the reconciliation date (00:00-24:00 UTC)
        Instant startOfDay = reconciliationDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = reconciliationDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Transaction> transactions =
                transactionQueryPort.findByMerchantIdAndCreatedAtBetween(merchantId, startOfDay, endOfDay);

        // Match transactions: CAPTURED/SETTLED are matched, FAILED/PENDING are discrepancies (stale/stuck)
        int totalTransactions = transactions.size();
        int matched = (int) transactions.stream()
                .filter(t -> MATCHED_STATUSES.contains(t.getStatus()))
                .count();
        int discrepancies = (int) transactions.stream()
                .filter(t -> DISCREPANCY_STATUSES.contains(t.getStatus()))
                .count();
        long totalAmountCents = sumAmountInCents(transactions, t -> true);
        long matchedAmountCents = sumAmountInCents(transactions, t -> MATCHED_STATUSES.contains(t.getStatus()));

        // Record real results on the batch (amounts are stored in cents)
        reconciliationDomainService.recordReconciliationResults(batch.getId(), totalTransactions, matched,
                discrepancies, BigDecimal.valueOf(totalAmountCents), BigDecimal.valueOf(matchedAmountCents));

        // Complete reconciliation
        batch = reconciliationDomainService.completeReconciliation(batch.getId());

        log.info("Reconciliation completed for batch: {} (total={}, matched={}, discrepancies={})",
                batch.getId(), totalTransactions, matched, discrepancies);

        return mapToResponse(batch);
    }

    private long sumAmountInCents(List<Transaction> transactions,
                                   java.util.function.Predicate<Transaction> filter) {
        return transactions.stream()
                .filter(filter)
                .filter(t -> t.getAmount() != null)
                .mapToLong(t -> t.getAmount().getAmountInCents())
                .sum();
    }

    private ReconciliationResponse mapToResponse(ReconciliationBatch batch) {
        BigDecimal totalAmount = batch.getTotalAmount() != null ? batch.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal matchedAmount = batch.getMatchedAmount() != null ? batch.getMatchedAmount() : BigDecimal.ZERO;
        return ReconciliationResponse.builder()
                .batchId(batch.getId())
                .status(batch.getStatus().name())
                .totalTransactions(batch.getTotalTransactions())
                .matchedCount(batch.getMatchedTransactions())
                .discrepancyCount(batch.getDiscrepancyCount())
                .totalAmount(totalAmount.longValueExact())
                .reconciledAmount(matchedAmount.longValueExact())
                // derived: the persistence model does not store the unmatched amount
                .discrepancyAmount(totalAmount.subtract(matchedAmount).longValueExact())
                .createdAt(batch.getCreatedAt())
                .completedAt(batch.getCompletedAt())
                .build();
    }
}
