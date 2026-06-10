package com.payment.gateway.infrastructure.merchant.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for MerchantJpaEntity.
 */
public interface MerchantJpaRepository extends JpaRepository<MerchantJpaEntity, String> {

    Optional<MerchantJpaEntity> findByEmail(String email);

    Optional<MerchantJpaEntity> findByApiKeyHash(String apiKeyHash);

    Optional<MerchantJpaEntity> findByApiKey(String apiKey);

    boolean existsByEmail(String email);

    boolean existsByApiKeyHash(String apiKeyHash);
}
