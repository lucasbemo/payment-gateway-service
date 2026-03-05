package com.payment.gateway.infrastructure.customer.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data JPA repository for PaymentMethodJpaEntity.
 */
public interface PaymentMethodJpaRepository extends JpaRepository<PaymentMethodJpaEntity, String> {

    @Query("SELECT pm FROM PaymentMethodJpaEntity pm WHERE pm.id = :id AND pm.customerId = :customerId")
    Optional<PaymentMethodJpaEntity> findByIdAndCustomerId(@Param("id") String id, @Param("customerId") String customerId);

    Optional<PaymentMethodJpaEntity> findByToken(String token);
}
