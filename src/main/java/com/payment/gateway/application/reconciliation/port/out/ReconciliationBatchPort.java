package com.payment.gateway.application.reconciliation.port.out;

import com.payment.gateway.domain.reconciliation.model.ReconciliationBatch;

import java.util.Optional;

/**
 * Output port for reconciliation operations.
 */
public interface ReconciliationBatchPort {

    ReconciliationBatch saveBatch(ReconciliationBatch batch);

    Optional<ReconciliationBatch> findById(String id);
}
