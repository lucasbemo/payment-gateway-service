package com.payment.gateway.infrastructure.idempotency.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyJpaEntity, String> {

    Optional<IdempotencyKeyJpaEntity> findByKeyHash(String keyHash);

    boolean existsByKeyHash(String keyHash);
}
