package com.payment.gateway.infrastructure.payment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * Spring Data JPA repository for PaymentJpaEntity.
 */
public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, String> {

    /**
     * Find a payment by idempotency key with pessimistic locking.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.idempotencyKey = :idempotencyKey")
    Optional<PaymentJpaEntity> findByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    /**
     * Check if a payment exists by idempotency key.
     */
    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Find all payments by merchant ID.
     */
    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.merchantId = :merchantId ORDER BY p.createdAt DESC")
    java.util.List<PaymentJpaEntity> findByMerchantId(@Param("merchantId") String merchantId);
}
