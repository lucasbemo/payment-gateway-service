package com.payment.gateway.infrastructure.idempotency.adapter.out.persistence;

import com.payment.gateway.domain.idempotency.model.IdempotencyKey;
import com.payment.gateway.domain.idempotency.port.IdempotencyKeyRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IdempotencyKeyPersistenceAdapter implements IdempotencyKeyRepositoryPort {

    private final IdempotencyKeyJpaRepository idempotencyKeyJpaRepository;
    private final IdempotencyKeyMapper idempotencyKeyMapper;

    @Override
    public IdempotencyKey save(IdempotencyKey idempotencyKey) {
        var entity = idempotencyKeyMapper.toEntity(idempotencyKey);
        var saved = idempotencyKeyJpaRepository.save(entity);
        return idempotencyKeyMapper.toDomain(saved);
    }

    @Override
    public Optional<IdempotencyKey> findByIdempotencyKey(String idempotencyKey) {
        return idempotencyKeyJpaRepository.findByKeyHash(idempotencyKey)
                .map(idempotencyKeyMapper::toDomain);
    }

    @Override
    public Optional<IdempotencyKey> findById(String id) {
        return idempotencyKeyJpaRepository.findById(id)
                .map(idempotencyKeyMapper::toDomain);
    }

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return idempotencyKeyJpaRepository.existsByKeyHash(idempotencyKey);
    }

    @Override
    public void deleteById(String id) {
        idempotencyKeyJpaRepository.deleteById(id);
    }
}
