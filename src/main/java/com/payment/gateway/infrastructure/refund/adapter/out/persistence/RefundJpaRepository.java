package com.payment.gateway.infrastructure.refund.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefundJpaRepository extends JpaRepository<RefundJpaEntity, String> {

    Optional<RefundJpaEntity> findByRefundIdempotencyKey(String refundIdempotencyKey);

    boolean existsByRefundIdempotencyKey(String refundIdempotencyKey);

    Optional<RefundJpaEntity> findFirstByPaymentId(String paymentId);

    Optional<RefundJpaEntity> findByTransactionId(String transactionId);

    List<RefundJpaEntity> findAllByPaymentId(String paymentId);

    List<RefundJpaEntity> findByMerchantId(String merchantId);

    List<RefundJpaEntity> findByStatus(String status);
}
