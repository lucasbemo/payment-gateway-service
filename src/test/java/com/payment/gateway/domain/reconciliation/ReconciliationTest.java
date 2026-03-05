package com.payment.gateway.domain.reconciliation;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.reconciliation.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Reconciliation domain aggregates.
 */
class ReconciliationTest {

    private static final String MERCHANT_ID = "merch_123";
    private static final LocalDate RECONCILIATION_DATE = LocalDate.now().minusDays(1);
    private static final String GATEWAY_NAME = "Stripe";
    private static final String INITIATED_BY = "system";

    private ReconciliationBatch batch;

    @BeforeEach
    void setUp() {
        batch = ReconciliationBatch.create(MERCHANT_ID, RECONCILIATION_DATE, GATEWAY_NAME, INITIATED_BY);
    }

    @Nested
    @DisplayName("ReconciliationBatch Creation")
    class BatchCreation {

        @Test
        @DisplayName("Should create batch with PENDING status")
        void shouldCreateBatchWithPendingStatus() {
            assertNotNull(batch.getId());
            assertEquals(MERCHANT_ID, batch.getMerchantId());
            assertEquals(RECONCILIATION_DATE, batch.getReconciliationDate());
            assertEquals(GATEWAY_NAME, batch.getGatewayName());
            assertEquals(ReconciliationStatus.PENDING, batch.getStatus());
            assertEquals(0, batch.getTotalTransactions());
        }
    }

    @Nested
    @DisplayName("ReconciliationBatch Processing")
    class BatchProcessing {

        @Test
        @DisplayName("Should start processing")
        void shouldStartProcessing() {
            batch.startProcessing();

            assertEquals(ReconciliationStatus.PROCESSING, batch.getStatus());
            assertNotNull(batch.getStartedAt());
        }

        @Test
        @DisplayName("Should update counts")
        void shouldUpdateCounts() {
            batch.updateCounts(100, 95, 5);

            assertEquals(100, batch.getTotalTransactions());
            assertEquals(95, batch.getMatchedTransactions());
            assertEquals(5, batch.getUnmatchedTransactions());
        }

        @Test
        @DisplayName("Should update amounts")
        void shouldUpdateAmounts() {
            BigDecimal total = BigDecimal.valueOf(10000);
            BigDecimal matched = BigDecimal.valueOf(9500);
            BigDecimal unmatched = BigDecimal.valueOf(500);

            batch.updateAmounts(total, matched, unmatched);

            assertEquals(total, batch.getTotalAmount());
            assertEquals(matched, batch.getMatchedAmount());
            assertEquals(unmatched, batch.getUnmatchedAmount());
        }

        @Test
        @DisplayName("Should increment discrepancy count")
        void shouldIncrementDiscrepancyCount() {
            batch.incrementDiscrepancyCount();
            batch.incrementDiscrepancyCount();

            assertEquals(2, batch.getDiscrepancyCount());
        }

        @Test
        @DisplayName("Should complete batch as COMPLETED when all matched")
        void shouldCompleteBatchAsCompleted() {
            batch.updateCounts(100, 100, 0);
            batch.complete();

            assertEquals(ReconciliationStatus.COMPLETED, batch.getStatus());
        }

        @Test
        @DisplayName("Should complete batch as PARTIALLY_RECONCILED when some unmatched")
        void shouldCompleteBatchAsPartiallyReconciled() {
            batch.updateCounts(100, 95, 5);
            batch.complete();

            assertEquals(ReconciliationStatus.PARTIALLY_RECONCILED, batch.getStatus());
        }

        @Test
        @DisplayName("Should fail batch")
        void shouldFailBatch() {
            batch.fail("Processing error");

            assertEquals(ReconciliationStatus.FAILED, batch.getStatus());
        }
    }

    @Nested
    @DisplayName("Discrepancy Creation")
    class DiscrepancyCreation {

        @Test
        @DisplayName("Should create discrepancy with OPEN status")
        void shouldCreateDiscrepancyWithOpenStatus() {
            Discrepancy discrepancy = Discrepancy.create(
                batch.getId(), MERCHANT_ID, DiscrepancyType.AMOUNT_MISMATCH, "txn_123", "Amount differs by $10"
            );

            assertNotNull(discrepancy.getId());
            assertEquals(DiscrepancyStatus.OPEN, discrepancy.getStatus());
            assertEquals(DiscrepancyType.AMOUNT_MISMATCH, discrepancy.getType());
        }

        @Test
        @DisplayName("Should set amounts on discrepancy")
        void shouldSetAmountsOnDiscrepancy() {
            Discrepancy discrepancy = Discrepancy.create(
                batch.getId(), MERCHANT_ID, DiscrepancyType.AMOUNT_MISMATCH, "txn_123", "Amount differs"
            );
            Money systemAmount = Money.of(BigDecimal.valueOf(100.00), Currency.getInstance("USD"));
            Money gatewayAmount = Money.of(BigDecimal.valueOf(90.00), Currency.getInstance("USD"));

            discrepancy.setAmounts(systemAmount, gatewayAmount);

            assertEquals(systemAmount, discrepancy.getSystemAmount());
            assertEquals(gatewayAmount, discrepancy.getGatewayAmount());
        }

        @Test
        @DisplayName("Should mark discrepancy under review")
        void shouldMarkDiscrepancyUnderReview() {
            Discrepancy discrepancy = Discrepancy.create(
                batch.getId(), MERCHANT_ID, DiscrepancyType.AMOUNT_MISMATCH, "txn_123", "Amount differs"
            );

            discrepancy.markUnderReview();

            assertEquals(DiscrepancyStatus.UNDER_REVIEW, discrepancy.getStatus());
        }

        @Test
        @DisplayName("Should resolve discrepancy")
        void shouldResolveDiscrepancy() {
            Discrepancy discrepancy = Discrepancy.create(
                batch.getId(), MERCHANT_ID, DiscrepancyType.AMOUNT_MISMATCH, "txn_123", "Amount differs"
            );

            discrepancy.resolve("Gateway fee adjustment", "admin");

            assertEquals(DiscrepancyStatus.RESOLVED, discrepancy.getStatus());
            assertEquals("admin", discrepancy.getResolvedBy());
            assertNotNull(discrepancy.getResolvedAt());
        }

        @Test
        @DisplayName("Should escalate discrepancy")
        void shouldEscalateDiscrepancy() {
            Discrepancy discrepancy = Discrepancy.create(
                batch.getId(), MERCHANT_ID, DiscrepancyType.AMOUNT_MISMATCH, "txn_123", "Amount differs"
            );

            discrepancy.escalate();

            assertEquals(DiscrepancyStatus.ESCALATED, discrepancy.getStatus());
        }

        @Test
        @DisplayName("Should close discrepancy")
        void shouldCloseDiscrepancy() {
            Discrepancy discrepancy = Discrepancy.create(
                batch.getId(), MERCHANT_ID, DiscrepancyType.AMOUNT_MISMATCH, "txn_123", "Amount differs"
            );

            discrepancy.close();

            assertEquals(DiscrepancyStatus.CLOSED, discrepancy.getStatus());
        }
    }

    @Nested
    @DisplayName("SettlementReport Creation")
    class SettlementReportCreation {

        @Test
        @DisplayName("Should create settlement report with PENDING status")
        void shouldCreateSettlementReportWithPendingStatus() {
            Money grossAmount = Money.of(BigDecimal.valueOf(10000.00), Currency.getInstance("USD"));
            Money feeAmount = Money.of(BigDecimal.valueOf(290.00), Currency.getInstance("USD"));
            Money netAmount = Money.of(BigDecimal.valueOf(9710.00), Currency.getInstance("USD"));

            SettlementReport report = SettlementReport.create(
                MERCHANT_ID, GATEWAY_NAME, RECONCILIATION_DATE, "report_123",
                grossAmount, feeAmount, netAmount, "USD"
            );

            assertNotNull(report.getId());
            assertEquals("PENDING", report.getStatus());
            assertEquals(grossAmount, report.getGrossAmount());
            assertEquals(feeAmount, report.getFeeAmount());
            assertEquals(netAmount, report.getNetAmount());
        }

        @Test
        @DisplayName("Should mark settlement as settled")
        void shouldMarkSettlementAsSettled() {
            Money grossAmount = Money.of(BigDecimal.valueOf(10000.00), Currency.getInstance("USD"));
            Money feeAmount = Money.of(BigDecimal.valueOf(290.00), Currency.getInstance("USD"));
            Money netAmount = Money.of(BigDecimal.valueOf(9710.00), Currency.getInstance("USD"));

            SettlementReport report = SettlementReport.create(
                MERCHANT_ID, GATEWAY_NAME, RECONCILIATION_DATE, "report_123",
                grossAmount, feeAmount, netAmount, "USD"
            );

            report.markSettled();

            assertEquals("SETTLED", report.getStatus());
            assertNotNull(report.getSettledAt());
        }

        @Test
        @DisplayName("Should link report to reconciliation batch")
        void shouldLinkReportToReconciliationBatch() {
            Money grossAmount = Money.of(BigDecimal.valueOf(10000.00), Currency.getInstance("USD"));
            Money feeAmount = Money.of(BigDecimal.valueOf(290.00), Currency.getInstance("USD"));
            Money netAmount = Money.of(BigDecimal.valueOf(9710.00), Currency.getInstance("USD"));

            SettlementReport report = SettlementReport.create(
                MERCHANT_ID, GATEWAY_NAME, RECONCILIATION_DATE, "report_123",
                grossAmount, feeAmount, netAmount, "USD"
            );

            report.linkToReconciliation("batch_456");

            assertEquals("batch_456", report.getReconciliationBatchId());
        }

        @Test
        @DisplayName("Should update file path")
        void shouldUpdateFilePath() {
            Money grossAmount = Money.of(BigDecimal.valueOf(10000.00), Currency.getInstance("USD"));
            Money feeAmount = Money.of(BigDecimal.valueOf(290.00), Currency.getInstance("USD"));
            Money netAmount = Money.of(BigDecimal.valueOf(9710.00), Currency.getInstance("USD"));

            SettlementReport report = SettlementReport.create(
                MERCHANT_ID, GATEWAY_NAME, RECONCILIATION_DATE, "report_123",
                grossAmount, feeAmount, netAmount, "USD"
            );

            report.updateFilePath("s3://bucket/reports/report_123.pdf");

            assertEquals("s3://bucket/reports/report_123.pdf", report.getFilePath());
        }
    }
}
