package com.payment.gateway.domain.reconciliation.service;

import com.payment.gateway.domain.reconciliation.model.*;
import com.payment.gateway.domain.reconciliation.port.DiscrepancyRepositoryPort;
import com.payment.gateway.domain.reconciliation.port.ReconciliationBatchRepositoryPort;
import com.payment.gateway.domain.reconciliation.port.SettlementReportRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reconciliation domain service.
 * Contains business logic for reconciliation operations.
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ReconciliationDomainService {

    private final ReconciliationBatchRepositoryPort batchRepository;
    private final DiscrepancyRepositoryPort discrepancyRepository;
    private final SettlementReportRepositoryPort settlementReportRepository;

    public ReconciliationBatch createReconciliationBatch(
            String merchantId, LocalDate reconciliationDate, String gatewayName, String initiatedBy) {
        log.info(
                "Creating reconciliation batch for merchant {} on date {} with gateway {}",
                merchantId,
                reconciliationDate,
                gatewayName);

        ReconciliationBatch batch =
                ReconciliationBatch.create(merchantId, reconciliationDate, gatewayName, initiatedBy);
        return batchRepository.save(batch);
    }

    /**
     * Returns the existing reconciliation batch for a merchant/gateway/date, or creates one if none
     * exists. Reconciliation is unique per merchant per date (constraint
     * {@code uk_reconciliation_batches_merchant_date}), so re-running for the same day reuses the
     * existing batch and recomputes its results instead of failing on a duplicate insert.
     */
    public ReconciliationBatch findOrCreateReconciliationBatch(
            String merchantId, LocalDate reconciliationDate, String gatewayName, String initiatedBy) {
        return batchRepository
                .findByMerchantIdAndGatewayNameAndDate(merchantId, gatewayName, reconciliationDate)
                .map(existing -> {
                    log.info(
                            "Reusing existing reconciliation batch {} for merchant {} on date {} (re-run)",
                            existing.getId(),
                            merchantId,
                            reconciliationDate);
                    return existing;
                })
                .orElseGet(() -> createReconciliationBatch(merchantId, reconciliationDate, gatewayName, initiatedBy));
    }

    public ReconciliationBatch startReconciliation(String batchId) {
        log.info("Starting reconciliation for batch {}", batchId);

        ReconciliationBatch batch = getBatchOrThrow(batchId);
        batch.startProcessing();
        return batchRepository.save(batch);
    }

    public ReconciliationBatch updateReconciliationProgress(String batchId, int total, int matched, int unmatched) {
        log.info(
                "Updating reconciliation progress for batch {}: total={}, matched={}, unmatched={}",
                batchId,
                total,
                matched,
                unmatched);

        ReconciliationBatch batch = getBatchOrThrow(batchId);
        batch.updateCounts(total, matched, unmatched);
        return batchRepository.save(batch);
    }

    public ReconciliationBatch recordReconciliationResults(
            String batchId,
            int total,
            int matched,
            int discrepancies,
            BigDecimal totalAmount,
            BigDecimal matchedAmount) {
        log.info(
                "Recording reconciliation results for batch {}: total={}, matched={}, discrepancies={}, totalAmount={}, matchedAmount={}",
                batchId,
                total,
                matched,
                discrepancies,
                totalAmount,
                matchedAmount);

        ReconciliationBatch batch = getBatchOrThrow(batchId);
        batch.updateCounts(total, matched, total - matched);
        batch.updateAmounts(totalAmount, matchedAmount, totalAmount.subtract(matchedAmount));
        batch.updateDiscrepancyCount(discrepancies);
        return batchRepository.save(batch);
    }

    public Discrepancy createDiscrepancy(
            String batchId, String merchantId, DiscrepancyType type, String transactionId, String description) {
        log.info("Creating discrepancy for batch {} transaction {}: {}", batchId, transactionId, type);

        Discrepancy discrepancy = Discrepancy.create(batchId, merchantId, type, transactionId, description);
        discrepancy = discrepancyRepository.save(discrepancy);

        ReconciliationBatch batch = getBatchOrThrow(batchId);
        batch.incrementDiscrepancyCount();
        batchRepository.save(batch);

        return discrepancy;
    }

    public Discrepancy resolveDiscrepancy(String discrepancyId, String resolutionNotes, String resolvedBy) {
        log.info("Resolving discrepancy {}", discrepancyId);

        Discrepancy discrepancy = getDiscrepancyOrThrow(discrepancyId);
        discrepancy.resolve(resolutionNotes, resolvedBy);
        return discrepancyRepository.save(discrepancy);
    }

    public Discrepancy escalateDiscrepancy(String discrepancyId) {
        log.info("Escalating discrepancy {}", discrepancyId);

        Discrepancy discrepancy = getDiscrepancyOrThrow(discrepancyId);
        discrepancy.escalate();
        return discrepancyRepository.save(discrepancy);
    }

    public ReconciliationBatch completeReconciliation(String batchId) {
        log.info("Completing reconciliation for batch {}", batchId);

        ReconciliationBatch batch = getBatchOrThrow(batchId);
        batch.complete();
        return batchRepository.save(batch);
    }

    public SettlementReport createSettlementReport(
            String merchantId,
            String gatewayName,
            LocalDate settlementDate,
            String gatewayReportId,
            com.payment.gateway.commons.model.Money grossAmount,
            com.payment.gateway.commons.model.Money feeAmount,
            com.payment.gateway.commons.model.Money netAmount,
            String currency) {
        log.info(
                "Creating settlement report for merchant {} gateway {} date {}",
                merchantId,
                gatewayName,
                settlementDate);

        SettlementReport report = SettlementReport.create(
                merchantId, gatewayName, settlementDate, gatewayReportId, grossAmount, feeAmount, netAmount, currency);
        return settlementReportRepository.save(report);
    }

    public SettlementReport markSettlementAsSettled(String reportId) {
        log.info("Marking settlement report {} as settled", reportId);

        SettlementReport report = getSettlementReportOrThrow(reportId);
        report.markSettled();
        return settlementReportRepository.save(report);
    }

    public ReconciliationBatch getBatchOrThrow(String batchId) {
        return batchRepository
                .findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Reconciliation batch not found: " + batchId));
    }

    public Discrepancy getDiscrepancyOrThrow(String discrepancyId) {
        return discrepancyRepository
                .findById(discrepancyId)
                .orElseThrow(() -> new IllegalArgumentException("Discrepancy not found: " + discrepancyId));
    }

    public SettlementReport getSettlementReportOrThrow(String reportId) {
        return settlementReportRepository
                .findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement report not found: " + reportId));
    }

    public List<ReconciliationBatch> getBatchesByMerchant(String merchantId) {
        return batchRepository.findByMerchantId(merchantId);
    }

    public List<Discrepancy> getDiscrepanciesByBatch(String batchId) {
        return discrepancyRepository.findByBatchId(batchId);
    }

    public List<Discrepancy> getOpenDiscrepancies() {
        return discrepancyRepository.findByStatus(DiscrepancyStatus.OPEN);
    }
}
