package com.payment.gateway.infrastructure.merchant.adapter.out.persistence;

import com.payment.gateway.application.payment.port.out.MerchantQueryPort;
import com.payment.gateway.domain.merchant.model.Merchant;
import com.payment.gateway.domain.merchant.port.MerchantRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for Merchant repository.
 * Implements both the domain's MerchantRepositoryPort and application's MerchantQueryPort interfaces.
 */
@Component
@RequiredArgsConstructor
public class MerchantPersistenceAdapter implements MerchantRepositoryPort, MerchantQueryPort {

    private final MerchantJpaRepository merchantJpaRepository;
    private final MerchantMapper merchantMapper;

    @Override
    public Merchant save(Merchant merchant) {
        var entity = merchantMapper.toEntity(merchant);
        var savedEntity = merchantJpaRepository.save(entity);
        return merchantMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Merchant> findById(String id) {
        return merchantJpaRepository.findById(id).map(merchantMapper::toDomain);
    }

    @Override
    public Optional<Merchant> findByEmail(String email) {
        return merchantJpaRepository.findByEmail(email).map(merchantMapper::toDomain);
    }

    @Override
    public Optional<Merchant> findByApiKeyHash(String apiKeyHash) {
        return merchantJpaRepository.findByApiKeyHash(apiKeyHash).map(merchantMapper::toDomain);
    }

    @Override
    public List<Merchant> findAll() {
        return merchantJpaRepository.findAll().stream()
                .map(merchantMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return merchantJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByApiKeyHash(String apiKeyHash) {
        return merchantJpaRepository.existsByApiKeyHash(apiKeyHash);
    }

    @Override
    public boolean existsById(String id) {
        return merchantJpaRepository.existsById(id);
    }
}
