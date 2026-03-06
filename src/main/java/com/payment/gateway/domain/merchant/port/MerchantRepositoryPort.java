package com.payment.gateway.domain.merchant.port;

import com.payment.gateway.domain.merchant.model.Merchant;

import java.util.List;
import java.util.Optional;

/**
 * Port for Merchant persistence operations.
 */
public interface MerchantRepositoryPort {

    Merchant save(Merchant merchant);

    Optional<Merchant> findById(String id);

    Optional<Merchant> findByEmail(String email);

    Optional<Merchant> findByApiKeyHash(String apiKeyHash);

    Optional<Merchant> findByApiKey(String apiKey);

    List<Merchant> findAll();

    boolean existsByEmail(String email);

    boolean existsByApiKeyHash(String apiKeyHash);
}
