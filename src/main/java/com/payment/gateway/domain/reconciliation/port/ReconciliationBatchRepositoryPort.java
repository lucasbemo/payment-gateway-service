package com.payment.gateway.domain.reconciliation.port;

import com.payment.gateway.domain.reconciliation.model.ReconciliationBatch;
import com.payment.gateway.domain.reconciliation.model.ReconciliationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Reconciliation batch repository port interface.
 */
public interface ReconciliationBatchRepositoryPort {
    ReconciliationBatch save(ReconciliationBatch batch);
    Optional<ReconciliationBatch> findById(String id);
    List<ReconciliationBatch> findByMerchantId(String merchantId);
    List<ReconciliationBatch> findByStatus(ReconciliationStatus status);
    List<ReconciliationBatch> findByReconciliationDate(LocalDate date);
    Optional<ReconciliationBatch> findByMerchantIdAndGatewayNameAndDate(String merchantId, String gatewayName, LocalDate date);
    void deleteById(String id);
}
