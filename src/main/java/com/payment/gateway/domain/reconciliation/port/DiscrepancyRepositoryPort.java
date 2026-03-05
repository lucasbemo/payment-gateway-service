package com.payment.gateway.domain.reconciliation.port;

import com.payment.gateway.domain.reconciliation.model.Discrepancy;
import com.payment.gateway.domain.reconciliation.model.DiscrepancyStatus;

import java.util.List;
import java.util.Optional;

/**
 * Discrepancy repository port interface.
 */
public interface DiscrepancyRepositoryPort {
    Discrepancy save(Discrepancy discrepancy);
    Optional<Discrepancy> findById(String id);
    List<Discrepancy> findByBatchId(String batchId);
    List<Discrepancy> findByMerchantId(String merchantId);
    List<Discrepancy> findByStatus(DiscrepancyStatus status);
    List<Discrepancy> findByTransactionId(String transactionId);
    void deleteById(String id);
}
