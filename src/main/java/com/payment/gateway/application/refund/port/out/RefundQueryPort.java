package com.payment.gateway.application.refund.port.out;

import com.payment.gateway.domain.refund.model.Refund;

import java.util.Optional;

/**
 * Output port for refund queries.
 */
public interface RefundQueryPort {

    Refund saveRefund(Refund refund);

    Optional<Refund> findById(String id);

    Optional<Refund> findByIdempotencyKey(String refundIdempotencyKey);

    boolean existsByIdempotencyKey(String refundIdempotencyKey);
}
