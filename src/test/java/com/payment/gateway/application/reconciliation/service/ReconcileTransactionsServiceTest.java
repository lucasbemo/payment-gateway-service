package com.payment.gateway.application.reconciliation.service;

import com.payment.gateway.application.reconciliation.dto.ReconciliationResponse;
import com.payment.gateway.application.reconciliation.port.out.ReconciliationBatchPort;
import com.payment.gateway.domain.reconciliation.model.ReconciliationBatch;
import com.payment.gateway.domain.reconciliation.model.ReconciliationStatus;
import com.payment.gateway.domain.reconciliation.service.ReconciliationDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("Reconcile Transactions Service Tests")
@ExtendWith(MockitoExtension.class)
class ReconcileTransactionsServiceTest {

    @Mock
    private ReconciliationBatchPort reconciliationBatchPort;

    @Mock
    private ReconciliationDomainService reconciliationDomainService;

    private ReconcileTransactionsService reconcileTransactionsService;

    @BeforeEach
    void setUp() {
        reconcileTransactionsService = new ReconcileTransactionsService(reconciliationBatchPort, reconciliationDomainService);
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
            given(reconciliationDomainService.completeReconciliation(batchId)).willReturn(batch);

            // When
            ReconciliationResponse response = reconcileTransactionsService.reconcileTransactions(merchantId, date);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getBatchId()).isEqualTo(batchId);

            then(reconciliationDomainService).should().createReconciliationBatch(any(), any(), any(), any());
            then(reconciliationDomainService).should().startReconciliation(batchId);
            then(reconciliationDomainService).should().completeReconciliation(batchId);
        }
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
