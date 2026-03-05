package com.payment.gateway.domain.refund.port;

import com.payment.gateway.domain.refund.model.Refund;
import com.payment.gateway.domain.refund.model.RefundStatus;

import java.util.List;
import java.util.Optional;

/**
 * Refund repository port interface.
 */
public interface RefundRepositoryPort {
    Refund save(Refund refund);
    Optional<Refund> findById(String id);
    Optional<Refund> findFirstByPaymentId(String paymentId);
    Optional<Refund> findByTransactionId(String transactionId);
    List<Refund> findAllByPaymentId(String paymentId);
    List<Refund> findByMerchantId(String merchantId);
    List<Refund> findByStatus(RefundStatus status);
    boolean existsByRefundIdempotencyKey(String refundIdempotencyKey);
    void deleteById(String id);
}
