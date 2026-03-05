package com.payment.gateway.application.payment.port.out;

import com.payment.gateway.domain.payment.model.Payment;

import java.util.Optional;

/**
 * Output port for payment persistence operations.
 * Extends the domain port with additional application-level queries if needed.
 */
public interface PaymentQueryPort {

    Payment savePayment(Payment payment);

    Optional<Payment> findById(String id);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
