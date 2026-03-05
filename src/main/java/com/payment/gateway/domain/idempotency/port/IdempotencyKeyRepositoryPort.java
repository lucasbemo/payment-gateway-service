package com.payment.gateway.domain.idempotency.port;

import com.payment.gateway.domain.idempotency.model.IdempotencyKey;

import java.util.Optional;

/**
 * Idempotency key repository port interface.
 */
public interface IdempotencyKeyRepositoryPort {
    IdempotencyKey save(IdempotencyKey idempotencyKey);
    Optional<IdempotencyKey> findByIdempotencyKey(String idempotencyKey);
    Optional<IdempotencyKey> findById(String id);
    boolean existsByIdempotencyKey(String idempotencyKey);
    void deleteById(String id);
}
