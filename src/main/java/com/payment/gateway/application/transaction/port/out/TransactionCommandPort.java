package com.payment.gateway.application.transaction.port.out;

import com.payment.gateway.domain.transaction.model.Transaction;

/**
 * Output port for transaction commands.
 */
public interface TransactionCommandPort {

    Transaction createTransaction(Transaction transaction);

    Transaction updateTransaction(Transaction transaction);
}
