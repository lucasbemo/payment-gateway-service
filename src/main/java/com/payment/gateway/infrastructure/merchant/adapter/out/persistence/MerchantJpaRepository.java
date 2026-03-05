package com.payment.gateway.infrastructure.merchant.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data JPA repository for MerchantJpaEntity.
 */
public interface MerchantJpaRepository extends JpaRepository<MerchantJpaEntity, String> {

    Optional<MerchantJpaEntity> findByEmail(String email);

    Optional<MerchantJpaEntity> findByApiKeyHash(String apiKeyHash);

    boolean existsByEmail(String email);

    boolean existsByApiKeyHash(String apiKeyHash);
}
