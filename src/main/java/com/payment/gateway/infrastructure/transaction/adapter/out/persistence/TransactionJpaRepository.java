package com.payment.gateway.infrastructure.transaction.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, String> {

    List<TransactionJpaEntity> findByPaymentId(String paymentId);

    Optional<TransactionJpaEntity> findByIdAndMerchantId(String id, String merchantId);

    @Query("SELECT t FROM TransactionJpaEntity t WHERE t.paymentId = :paymentId ORDER BY t.createdAt DESC LIMIT 1")
    Optional<TransactionJpaEntity> findLatestByPaymentId(@Param("paymentId") String paymentId);

    Optional<TransactionJpaEntity> findByPaymentIdAndType(String paymentId, String type);

    List<TransactionJpaEntity> findByMerchantId(String merchantId);

    List<TransactionJpaEntity> findByStatus(String status);

    List<TransactionJpaEntity> findByPaymentIdAndStatus(String paymentId, String status);

    boolean existsByPaymentId(String paymentId);
}
