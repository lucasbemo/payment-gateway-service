package com.payment.gateway.domain.transaction.port;

import com.payment.gateway.domain.transaction.model.Transaction;

/**
 * Transaction event publisher port interface.
 */
public interface TransactionEventPublisherPort {
    void publishTransactionCreated(Transaction transaction);
    void publishTransactionCompleted(Transaction transaction);
    void publishTransactionFailed(Transaction transaction);
}
