package com.payment.gateway.domain.payment.port;

import com.payment.gateway.domain.payment.model.Payment;

import java.util.List;
import java.util.Optional;

/**
 * Port for Payment persistence operations.
 */
public interface PaymentRepositoryPort {

    Payment save(Payment payment);

    Optional<Payment> findById(String id);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByMerchantId(String merchantId);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
