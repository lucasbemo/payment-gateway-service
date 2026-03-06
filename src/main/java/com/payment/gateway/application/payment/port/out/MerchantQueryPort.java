package com.payment.gateway.application.payment.port.out;

import com.payment.gateway.domain.merchant.model.Merchant;

import java.util.Optional;

/**
 * Output port for merchant queries.
 */
public interface MerchantQueryPort {

    Optional<Merchant> findById(String id);

    boolean existsById(String id);

    Optional<Merchant> findByApiKey(String apiKey);
}
