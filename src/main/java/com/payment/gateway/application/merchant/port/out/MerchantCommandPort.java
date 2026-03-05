package com.payment.gateway.application.merchant.port.out;

import com.payment.gateway.domain.merchant.model.Merchant;

import java.util.Optional;

/**
 * Output port for merchant operations.
 */
public interface MerchantCommandPort {

    Merchant saveMerchant(Merchant merchant);

    Optional<Merchant> findById(String id);

    Optional<Merchant> findByEmail(String email);

    boolean existsByEmail(String email);
}
