package com.payment.gateway.infrastructure.refund.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundJpaRepository extends JpaRepository<RefundJpaEntity, String> {

    Optional<RefundJpaEntity> findByRefundIdempotencyKey(String refundIdempotencyKey);

    boolean existsByRefundIdempotencyKey(String refundIdempotencyKey);
}
