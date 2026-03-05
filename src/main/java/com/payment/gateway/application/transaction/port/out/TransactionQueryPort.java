package com.payment.gateway.application.transaction.port.out;

import com.payment.gateway.domain.transaction.model.Transaction;

import java.util.Optional;

/**
 * Output port for transaction queries.
 */
public interface TransactionQueryPort {

    Optional<Transaction> findById(String id);

    Optional<Transaction> findByIdAndMerchantId(String id, String merchantId);
}
