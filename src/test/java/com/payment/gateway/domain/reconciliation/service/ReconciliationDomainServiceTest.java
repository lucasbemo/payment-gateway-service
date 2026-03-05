package com.payment.gateway.domain.reconciliation.service;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.reconciliation.model.*;
import com.payment.gateway.domain.reconciliation.port.DiscrepancyRepositoryPort;
import com.payment.gateway.domain.reconciliation.port.ReconciliationBatchRepositoryPort;
import com.payment.gateway.domain.reconciliation.port.SettlementReportRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReconciliationDomainService Tests")
class ReconciliationDomainServiceTest {

    @Mock
    private ReconciliationBatchRepositoryPort batchRepository;

    @Mock
    private DiscrepancyRepositoryPort discrepancyRepository;

    @Mock
    private SettlementReportRepositoryPort settlementReportRepository;

    private ReconciliationDomainService reconciliationDomainService;

    private final String BATCH_ID = "batch_123";
    private final String MERCHANT_ID = "merch_123";
    private final String DISCREPANCY_ID = "disc_123";
    private final String REPORT_ID = "report_123";
    private final String GATEWAY_NAME = "STRIPE";
    private final LocalDate RECONCILIATION_DATE = LocalDate.of(2024, 1, 15);
    private final String INITIATED_BY = "system";
    private final String TRANSACTION_ID = "txn_123";
    private final Currency BRL = Currency.getInstance("BRL");

    @BeforeEach
    void setUp() {
        reconciliationDomainService = new ReconciliationDomainService(batchRepository, discrepancyRepository, settlementReportRepository);
    }

    @Nested
    @DisplayName("Create Reconciliation Batch")
    class CreateReconciliationBatchTests {

        @Test
        @DisplayName("Should create reconciliation batch successfully")
        void shouldCreateReconciliationBatchSuccessfully() {
            // Given
            ReconciliationBatch batch = ReconciliationBatch.create(MERCHANT_ID, RECONCILIATION_DATE, GATEWAY_NAME, INITIATED_BY);
            given(batchRepository.save(any(ReconciliationBatch.class))).willReturn(batch);

            // When
            ReconciliationBatch result = reconciliationDomainService.createReconciliationBatch(
                MERCHANT_ID, RECONCILIATION_DATE, GATEWAY_NAME, INITIATED_BY
            );

            // Then
            assertThat(result).isNotNull();
            verify(batchRepository).save(any(ReconciliationBatch.class));
        }
    }

    @Nested
    @DisplayName("Start Reconciliation")
    class StartReconciliationTests {

        @Test
        @DisplayName("Should start reconciliation successfully")
        void shouldStartReconciliationSuccessfully() {
            // Given
            ReconciliationBatch batch = ReconciliationBatch.create(MERCHANT_ID, RECONCILIATION_DATE, GATEWAY_NAME, INITIATED_BY);
            given(batchRepository.findById(BATCH_ID)).willReturn(Optional.of(batch));
            given(batchRepository.save(any(ReconciliationBatch.class))).willReturn(batch);

            // When
            ReconciliationBatch result = reconciliationDomainService.startReconciliation(BATCH_ID);

            // Then
            assertThat(result).isEqualTo(batch);
            verify(batchRepository).findById(BATCH_ID);
            verify(batchRepository).save(batch);
        }

        @Test
        @DisplayName("Should throw exception when batch not found")
        void shouldThrowExceptionWhenBatchNotFound() {
            // Given
            given(batchRepository.findById(BATCH_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> reconciliationDomainService.startReconciliation(BATCH_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reconciliation batch not found");
        }
    }

    @Nested
    @DisplayName("Update Reconciliation Progress")
    class UpdateReconciliationProgressTests {

        @Test
        @DisplayName("Should update reconciliation progress successfully")
        void shouldUpdateReconciliationProgressSuccessfully() {
            // Given
            ReconciliationBatch batch = ReconciliationBatch.create(MERCHANT_ID, RECONCILIATION_DATE, GATEWAY_NAME, INITIATED_BY);
            int total = 100;
            int matched = 95;
            int unmatched = 5;
            given(batchRepository.findById(BATCH_ID)).willReturn(Optional.of(batch));
            given(batchRepository.save(any(ReconciliationBatch.class))).willReturn(batch);

            // When
            ReconciliationBatch result = reconciliationDomainService.updateReconciliationProgress(
                BATCH_ID, total, matched, unmatched
            );

            // Then
            assertThat(result).isEqualTo(batch);
            verify(batchRepository).findById(BATCH_ID);
            verify(batchRepository).save(batch);
        }
    }

    @Nested
    @DisplayName("Create Discrepancy")
    class CreateDiscrepancyTests {

        @Test
        @DisplayName("Should create discrepancy successfully")
        void shouldCreateDiscrepancySuccessfully() {
            // Given
            ReconciliationBatch batch = ReconciliationBatch.create(MERCHANT_ID, RECONCILIATION_DATE, GATEWAY_NAME, INITIATED_BY);
            DiscrepancyType type = DiscrepancyType.AMOUNT_MISMATCH;
            String description = "Amount differs between systems";
            Discrepancy discrepancy = Discrepancy.create(BATCH_ID, MERCHANT_ID, type, TRANSACTION_ID, description);

            given(batchRepository.findById(BATCH_ID)).willReturn(Optional.of(batch));
            given(batchRepository.save(any(ReconciliationBatch.class))).willReturn(batch);
            given(discrepancyRepository.save(any(Discrepancy.class))).willReturn(discrepancy);

            // When
            Discrepancy result = reconciliationDomainService.createDiscrepancy(
                BATCH_ID, MERCHANT_ID, type, TRANSACTION_ID, description
            );

            // Then
            assertThat(result).isNotNull();
            verify(discrepancyRepository).save(any(Discrepancy.class));
            verify(batchRepository).save(batch);
        }

        @Test
        @DisplayName("Should increment discrepancy count on batch")
        void shouldIncrementDiscrepancyCountOnBatch() {
            // Given
            ReconciliationBatch batch = ReconciliationBatch.create(MERCHANT_ID, RECONCILIATION_DATE, GATEWAY_NAME, INITIATED_BY);
            DiscrepancyType type = DiscrepancyType.MISSING_IN_GATEWAY;
            String description = "Transaction not found in gateway";
            Discrepancy discrepancy = Discrepancy.create(BATCH_ID, MERCHANT_ID, type, TRANSACTION_ID, description);

            given(batchRepository.findById(BATCH_ID)).willReturn(Optional.of(batch));
            given(batchRepository.save(any(ReconciliationBatch.class))).willReturn(batch);
            given(discrepancyRepository.save(any(Discrepancy.class))).willReturn(discrepancy);

            // When
            reconciliationDomainService.createDiscrepancy(BATCH_ID, MERCHANT_ID, type, TRANSACTION_ID, description);

            // Then
            verify(batchRepository).save(batch);
        }
    }

    @Nested
    @DisplayName("Resolve Discrepancy")
    class ResolveDiscrepancyTests {

        @Test
        @DisplayName("Should resolve discrepancy successfully")
        void shouldResolveDiscrepancySuccessfully() {
            // Given
            Discrepancy discrepancy = Discrepancy.create(BATCH_ID, MERCHANT_ID, DiscrepancyType.AMOUNT_MISMATCH, TRANSACTION_ID, "Description");
            String resolutionNotes = "Adjusted manually";
            String resolvedBy = "admin";
            given(discrepancyRepository.findById(DISCREPANCY_ID)).willReturn(Optional.of(discrepancy));
            given(discrepancyRepository.save(any(Discrepancy.class))).willReturn(discrepancy);

            // When
            Discrepancy result = reconciliationDomainService.resolveDiscrepancy(DISCREPANCY_ID, resolutionNotes, resolvedBy);

            // Then
            assertThat(result).isEqualTo(discrepancy);
            assertThat(result.getStatus()).isEqualTo(DiscrepancyStatus.RESOLVED);
            verify(discrepancyRepository).findById(DISCREPANCY_ID);
            verify(discrepancyRepository).save(discrepancy);
        }

        @Test
        @DisplayName("Should throw exception when discrepancy not found")
        void shouldThrowExceptionWhenDiscrepancyNotFound() {
            // Given
            given(discrepancyRepository.findById(DISCREPANCY_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> reconciliationDomainService.resolveDiscrepancy(DISCREPANCY_ID, "Notes", "Resolver"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Discrepancy not found");
        }
    }

    @Nested
    @DisplayName("Escalate Discrepancy")
    class EscalateDiscrepancyTests {

        @Test
        @DisplayName("Should escalate discrepancy successfully")
        void shouldEscalateDiscrepancySuccessfully() {
            // Given
            Discrepancy discrepancy = Discrepancy.create(BATCH_ID, MERCHANT_ID, DiscrepancyType.AMOUNT_MISMATCH, TRANSACTION_ID, "Description");
            given(discrepancyRepository.findById(DISCREPANCY_ID)).willReturn(Optional.of(discrepancy));
            given(discrepancyRepository.save(any(Discrepancy.class))).willReturn(discrepancy);

            // When
            Discrepancy result = reconciliationDomainService.escalateDiscrepancy(DISCREPANCY_ID);

            // Then
            assertThat(result).isEqualTo(discrepancy);
            verify(discrepancyRepository).findById(DISCREPANCY_ID);
            verify(discrepancyRepository).save(discrepancy);
        }
    }

    @Nested
    @DisplayName("Complete Reconciliation")
    class CompleteReconciliationTests {

        @Test
        @DisplayName("Should complete reconciliation successfully")
        void shouldCompleteReconciliationSuccessfully() {
            // Given
            ReconciliationBatch batch = ReconciliationBatch.create(MERCHANT_ID, RECONCILIATION_DATE, GATEWAY_NAME, INITIATED_BY);
            given(batchRepository.findById(BATCH_ID)).willReturn(Optional.of(batch));
            given(batchRepository.save(any(ReconciliationBatch.class))).willReturn(batch);

            // When
            ReconciliationBatch result = reconciliationDomainService.completeReconciliation(BATCH_ID);

            // Then
            assertThat(result).isEqualTo(batch);
            assertThat(result.getStatus()).isEqualTo(ReconciliationStatus.COMPLETED);
            verify(batchRepository).findById(BATCH_ID);
            verify(batchRepository).save(batch);
        }
    }

    @Nested
    @DisplayName("Create Settlement Report")
    class CreateSettlementReportTests {

        @Test
        @DisplayName("Should create settlement report successfully")
        void shouldCreateSettlementReportSuccessfully() {
            // Given
            String gatewayReportId = "stripe_report_123";
            Money grossAmount = Money.of(new BigDecimal("10000.00"), BRL);
            Money feeAmount = Money.of(new BigDecimal("300.00"), BRL);
            Money netAmount = Money.of(new BigDecimal("9700.00"), BRL);
            String currency = "BRL";

            SettlementReport report = SettlementReport.create(
                MERCHANT_ID, GATEWAY_NAME, RECONCILIATION_DATE, gatewayReportId,
                grossAmount, feeAmount, netAmount, currency
            );
            given(settlementReportRepository.save(any(SettlementReport.class))).willReturn(report);

            // When
            SettlementReport result = reconciliationDomainService.createSettlementReport(
                MERCHANT_ID, GATEWAY_NAME, RECONCILIATION_DATE, gatewayReportId,
                grossAmount, feeAmount, netAmount, currency
            );

            // Then
            assertThat(result).isNotNull();
            verify(settlementReportRepository).save(any(SettlementReport.class));
        }
    }

    @Nested
    @DisplayName("Mark Settlement As Settled")
    class MarkSettlementAsSettledTests {

        @Test
        @DisplayName("Should mark settlement as settled successfully")
        void shouldMarkSettlementAsSettledSuccessfully() {
            // Given
            String gatewayReportId = "stripe_report_123";
            Money grossAmount = Money.of(new BigDecimal("10000.00"), BRL);
            Money feeAmount = Money.of(new BigDecimal("300.00"), BRL);
            Money netAmount = Money.of(new BigDecimal("9700.00"), BRL);
            String currency = "BRL";

            SettlementReport report = SettlementReport.create(
                MERCHANT_ID, GATEWAY_NAME, RECONCILIATION_DATE, gatewayReportId,
                grossAmount, feeAmount, netAmount, currency
            );
            given(settlementReportRepository.findById(REPORT_ID)).willReturn(Optional.of(report));
            given(settlementReportRepository.save(any(SettlementReport.class))).willReturn(report);

            // When
            SettlementReport result = reconciliationDomainService.markSettlementAsSettled(REPORT_ID);

            // Then
            assertThat(result).isEqualTo(report);
            verify(settlementReportRepository).findById(REPORT_ID);
            verify(settlementReportRepository).save(report);
        }

        @Test
        @DisplayName("Should throw exception when settlement report not found")
        void shouldThrowExceptionWhenSettlementReportNotFound() {
            // Given
            given(settlementReportRepository.findById(REPORT_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> reconciliationDomainService.markSettlementAsSettled(REPORT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Settlement report not found");
        }
    }

    @Nested
    @DisplayName("Get Batches By Merchant")
    class GetBatchesByMerchantTests {

        @Test
        @DisplayName("Should return list of batches by merchant ID")
        void shouldReturnListOfBatchesByMerchantId() {
            // Given
            ReconciliationBatch batch = ReconciliationBatch.create(MERCHANT_ID, RECONCILIATION_DATE, GATEWAY_NAME, INITIATED_BY);
            given(batchRepository.findByMerchantId(MERCHANT_ID)).willReturn(List.of(batch));

            // When
            List<ReconciliationBatch> result = reconciliationDomainService.getBatchesByMerchant(MERCHANT_ID);

            // Then
            assertThat(result).hasSize(1);
            verify(batchRepository).findByMerchantId(MERCHANT_ID);
        }
    }

    @Nested
    @DisplayName("Get Discrepancies By Batch")
    class GetDiscrepanciesByBatchTests {

        @Test
        @DisplayName("Should return list of discrepancies by batch ID")
        void shouldReturnListOfDiscrepanciesByBatchId() {
            // Given
            Discrepancy discrepancy = Discrepancy.create(BATCH_ID, MERCHANT_ID, DiscrepancyType.AMOUNT_MISMATCH, TRANSACTION_ID, "Description");
            given(discrepancyRepository.findByBatchId(BATCH_ID)).willReturn(List.of(discrepancy));

            // When
            List<Discrepancy> result = reconciliationDomainService.getDiscrepanciesByBatch(BATCH_ID);

            // Then
            assertThat(result).hasSize(1);
            verify(discrepancyRepository).findByBatchId(BATCH_ID);
        }
    }

    @Nested
    @DisplayName("Get Open Discrepancies")
    class GetOpenDiscrepanciesTests {

        @Test
        @DisplayName("Should return list of open discrepancies")
        void shouldReturnListOfOpenDiscrepancies() {
            // Given
            Discrepancy discrepancy = Discrepancy.create(BATCH_ID, MERCHANT_ID, DiscrepancyType.AMOUNT_MISMATCH, TRANSACTION_ID, "Description");
            given(discrepancyRepository.findByStatus(DiscrepancyStatus.OPEN)).willReturn(List.of(discrepancy));

            // When
            List<Discrepancy> result = reconciliationDomainService.getOpenDiscrepancies();

            // Then
            assertThat(result).hasSize(1);
            verify(discrepancyRepository).findByStatus(DiscrepancyStatus.OPEN);
        }
    }

    @Nested
    @DisplayName("Get Batch Or Throw")
    class GetBatchOrThrowTests {

        @Test
        @DisplayName("Should return batch when found")
        void shouldReturnBatchWhenFound() {
            // Given
            ReconciliationBatch batch = ReconciliationBatch.create(MERCHANT_ID, RECONCILIATION_DATE, GATEWAY_NAME, INITIATED_BY);
            given(batchRepository.findById(BATCH_ID)).willReturn(Optional.of(batch));

            // When
            ReconciliationBatch result = reconciliationDomainService.getBatchOrThrow(BATCH_ID);

            // Then
            assertThat(result).isEqualTo(batch);
            verify(batchRepository).findById(BATCH_ID);
        }

        @Test
        @DisplayName("Should throw exception when batch not found")
        void shouldThrowExceptionWhenBatchNotFound() {
            // Given
            given(batchRepository.findById(BATCH_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> reconciliationDomainService.getBatchOrThrow(BATCH_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reconciliation batch not found");
        }
    }

    @Nested
    @DisplayName("Get Discrepancy Or Throw")
    class GetDiscrepancyOrThrowTests {

        @Test
        @DisplayName("Should return discrepancy when found")
        void shouldReturnDiscrepancyWhenFound() {
            // Given
            Discrepancy discrepancy = Discrepancy.create(BATCH_ID, MERCHANT_ID, DiscrepancyType.AMOUNT_MISMATCH, TRANSACTION_ID, "Description");
            given(discrepancyRepository.findById(DISCREPANCY_ID)).willReturn(Optional.of(discrepancy));

            // When
            Discrepancy result = reconciliationDomainService.getDiscrepancyOrThrow(DISCREPANCY_ID);

            // Then
            assertThat(result).isEqualTo(discrepancy);
            verify(discrepancyRepository).findById(DISCREPANCY_ID);
        }

        @Test
        @DisplayName("Should throw exception when discrepancy not found")
        void shouldThrowExceptionWhenDiscrepancyNotFound() {
            // Given
            given(discrepancyRepository.findById(DISCREPANCY_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> reconciliationDomainService.getDiscrepancyOrThrow(DISCREPANCY_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Discrepancy not found");
        }
    }
}
