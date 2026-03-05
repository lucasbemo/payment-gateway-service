package com.payment.gateway.domain.transaction.port;

import com.payment.gateway.domain.transaction.model.Transaction;
import com.payment.gateway.domain.transaction.model.TransactionStatus;

import java.util.List;
import java.util.Optional;

/**
 * Transaction repository port interface.
 */
public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(String id);
    Optional<Transaction> findByPaymentIdAndType(String paymentId, String type);
    List<Transaction> findByPaymentId(String paymentId);
    List<Transaction> findByMerchantId(String merchantId);
    List<Transaction> findByStatus(TransactionStatus status);
    List<Transaction> findByPaymentIdAndStatus(String paymentId, TransactionStatus status);
    boolean existsByPaymentId(String paymentId);
    void deleteById(String id);
}
