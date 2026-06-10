package com.payment.gateway.application.reconciliation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.payment.gateway.application.reconciliation.dto.ReconciliationResponse;
import com.payment.gateway.application.reconciliation.port.out.ReconciliationBatchPort;
import com.payment.gateway.application.transaction.port.out.TransactionQueryPort;
import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.reconciliation.model.ReconciliationBatch;
import com.payment.gateway.domain.reconciliation.model.ReconciliationStatus;
import com.payment.gateway.domain.reconciliation.service.ReconciliationDomainService;
import com.payment.gateway.domain.transaction.model.Transaction;
import com.payment.gateway.domain.transaction.model.TransactionStatus;
import com.payment.gateway.domain.transaction.model.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("Reconcile Transactions Service Tests")
@ExtendWith(MockitoExtension.class)
class ReconcileTransactionsServiceTest {

    @Mock
    private ReconciliationBatchPort reconciliationBatchPort;

    @Mock
    private ReconciliationDomainService reconciliationDomainService;

    @Mock
    private TransactionQueryPort transactionQueryPort;

    private ReconcileTransactionsService reconcileTransactionsService;

    @BeforeEach
    void setUp() {
        reconcileTransactionsService = new ReconcileTransactionsService(
                reconciliationBatchPort, reconciliationDomainService, transactionQueryPort);
    }

    @Nested
    @DisplayName("Successful Reconciliation")
    class SuccessfulReconciliationTests {

        @Test
        @DisplayName("Should reconcile transactions successfully")
        void shouldReconcileTransactionsSuccessfully() {
            // Given
            String merchantId = "merchant_123";
            String date = LocalDate.now().toString();
            String batchId = "batch_123";

            ReconciliationBatch batch = createReconciliationBatch(batchId, merchantId);

            given(reconciliationDomainService.createReconciliationBatch(any(), any(), any(), any()))
                    .willReturn(batch);
            given(reconciliationDomainService.startReconciliation(batchId)).willReturn(batch);
            given(transactionQueryPort.findByMerchantIdAndCreatedAtBetween(eq(merchantId), any(), any()))
                    .willReturn(List.of(
                            createTransaction(merchantId, TransactionStatus.CAPTURED, 1000L),
                            createTransaction(merchantId, TransactionStatus.SETTLED, 2500L),
                            createTransaction(merchantId, TransactionStatus.FAILED, 500L)));
            given(reconciliationDomainService.completeReconciliation(batchId)).willReturn(batch);

            // When
            ReconciliationResponse response = reconcileTransactionsService.reconcileTransactions(merchantId, date);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getBatchId()).isEqualTo(batchId);

            then(reconciliationDomainService).should().createReconciliationBatch(any(), any(), any(), any());
            then(reconciliationDomainService).should().startReconciliation(batchId);
            // total=3, matched=2 (CAPTURED+SETTLED), discrepancies=1 (FAILED),
            // totalAmount=4000 cents, matchedAmount=3500 cents
            then(reconciliationDomainService)
                    .should()
                    .recordReconciliationResults(
                            batchId, 3, 2, 1, BigDecimal.valueOf(4000L), BigDecimal.valueOf(3500L));
            then(reconciliationDomainService).should().completeReconciliation(batchId);
        }

        @Test
        @DisplayName("Should reconcile with zero results when no transactions exist for the date")
        void shouldReconcileWithZeroResultsWhenNoTransactions() {
            // Given
            String merchantId = "merchant_123";
            String date = LocalDate.now().toString();
            String batchId = "batch_123";

            ReconciliationBatch batch = createReconciliationBatch(batchId, merchantId);

            given(reconciliationDomainService.createReconciliationBatch(any(), any(), any(), any()))
                    .willReturn(batch);
            given(reconciliationDomainService.startReconciliation(batchId)).willReturn(batch);
            given(transactionQueryPort.findByMerchantIdAndCreatedAtBetween(eq(merchantId), any(), any()))
                    .willReturn(List.of());
            given(reconciliationDomainService.completeReconciliation(batchId)).willReturn(batch);

            // When
            ReconciliationResponse response = reconcileTransactionsService.reconcileTransactions(merchantId, date);

            // Then
            assertThat(response).isNotNull();
            then(reconciliationDomainService)
                    .should()
                    .recordReconciliationResults(batchId, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }

    private Transaction createTransaction(String merchantId, TransactionStatus status, long amountInCents) {
        Instant now = Instant.now();
        return Transaction.builder()
                .id(java.util.UUID.randomUUID().toString())
                .paymentId("payment_123")
                .merchantId(merchantId)
                .type(TransactionType.PAYMENT)
                .amount(Money.of(amountInCents, Currency.getInstance("USD")))
                .currency("USD")
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private ReconciliationBatch createReconciliationBatch(String id, String merchantId) {
        LocalDate today = LocalDate.now();

        ReconciliationBatch batch = ReconciliationBatch.builder()
                .id(id)
                .merchantId(merchantId)
                .reconciliationDate(today)
                .gatewayName("TEST_GATEWAY")
                .status(ReconciliationStatus.COMPLETED)
                .totalTransactions(100)
                .matchedTransactions(95)
                .unmatchedTransactions(5)
                .totalAmount(java.math.BigDecimal.valueOf(100000))
                .matchedAmount(java.math.BigDecimal.valueOf(95000))
                .unmatchedAmount(java.math.BigDecimal.valueOf(5000))
                .discrepancyCount(5)
                .createdAt(java.time.Instant.now())
                .completedAt(java.time.Instant.now())
                .build();
        return batch;
    }
}
