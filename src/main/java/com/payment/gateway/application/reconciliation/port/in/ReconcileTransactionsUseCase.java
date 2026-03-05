package com.payment.gateway.application.reconciliation.port.in;

import com.payment.gateway.application.reconciliation.dto.ReconciliationResponse;

/**
 * Use case for reconciling transactions.
 */
public interface ReconcileTransactionsUseCase {

    ReconciliationResponse reconcileTransactions(String merchantId, String date);
}
