package com.payment.gateway.infrastructure.customer.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for CustomerJpaEntity.
 */
public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, String> {

    Optional<CustomerJpaEntity> findByIdAndMerchantId(String id, String merchantId);

    @Query("SELECT c FROM CustomerJpaEntity c WHERE c.email = :email AND c.merchantId = :merchantId")
    Optional<CustomerJpaEntity> findByEmailAndMerchantId(@Param("email") String email, @Param("merchantId") String merchantId);

    Optional<CustomerJpaEntity> findByEmail(String email);

    List<CustomerJpaEntity> findByMerchantId(String merchantId);

    List<CustomerJpaEntity> findByEmailContainingAndMerchantId(String email, String merchantId);

    boolean existsByEmail(String email);
}
