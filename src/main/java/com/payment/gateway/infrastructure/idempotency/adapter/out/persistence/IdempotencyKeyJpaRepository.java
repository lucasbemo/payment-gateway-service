package com.payment.gateway.infrastructure.idempotency.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyJpaEntity, String> {

    Optional<IdempotencyKeyJpaEntity> findByKeyHash(String keyHash);

    boolean existsByKeyHash(String keyHash);
}
