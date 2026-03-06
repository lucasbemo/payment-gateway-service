package com.payment.gateway.domain.idempotency.service;

import com.payment.gateway.commons.utils.CryptoUtils;
import com.payment.gateway.domain.idempotency.model.IdempotencyKey;
import com.payment.gateway.domain.idempotency.model.IdempotencyStatus;
import com.payment.gateway.domain.idempotency.port.IdempotencyKeyRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Idempotency domain service.
 * Contains business logic for idempotency operations.
 */
@Service
@Slf4j
@Transactional
public class IdempotencyDomainService {

    private final IdempotencyKeyRepositoryPort repository;
    private final int ttlSeconds;

    @Autowired
    public IdempotencyDomainService(IdempotencyKeyRepositoryPort repository) {
        this(repository, 86400); // Default 24 hours
    }

    public IdempotencyDomainService(IdempotencyKeyRepositoryPort repository, int ttlSeconds) {
        this.repository = repository;
        this.ttlSeconds = ttlSeconds;
    }

    public IdempotencyKey createOrGetIdempotencyKey(String idempotencyKey, String merchantId,
                                                      String operation, String requestBody) {
        log.info("Creating or getting idempotency key: {} for operation: {}", idempotencyKey, operation);

        String requestHash = CryptoUtils.hash(requestBody != null ? requestBody : "");

        Optional<IdempotencyKey> existing = repository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyKey key = existing.get();

            if (key.isExpired()) {
                key.expire();
                repository.save(key);
                throw new IllegalStateException("Idempotency key has expired: " + idempotencyKey);
            }

            if (key.isTerminal()) {
                log.info("Returning existing idempotency key result: {}", idempotencyKey);
                return key;
            }

            return key;
        }

        IdempotencyKey newKey = IdempotencyKey.create(idempotencyKey, merchantId, operation, requestHash, ttlSeconds);
        return repository.save(newKey);
    }

    public IdempotencyKey acquireLock(String idempotencyKey, String lockToken) {
        log.info("Acquiring lock for idempotency key: {}", idempotencyKey);

        IdempotencyKey key = repository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new IllegalArgumentException("Idempotency key not found: " + idempotencyKey));

        if (key.isTerminal()) {
            throw new IllegalStateException("Cannot lock terminal idempotency key: " + idempotencyKey);
        }

        if (key.isLocked() && !key.getLockToken().equals(lockToken)) {
            throw new IllegalStateException("Idempotency key is already locked: " + idempotencyKey);
        }

        key.lock(lockToken);
        return repository.save(key);
    }

    public IdempotencyKey completeWithResponse(String idempotencyKey, String lockToken,
                                                String responseCode, String responseBody) {
        log.info("Completing idempotency key with response: {}", idempotencyKey);

        IdempotencyKey key = repository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new IllegalArgumentException("Idempotency key not found: " + idempotencyKey));

        if (key.getLockToken() != null && !key.getLockToken().equals(lockToken)) {
            throw new IllegalStateException("Invalid lock token for idempotency key: " + idempotencyKey);
        }

        key.complete(responseCode, responseBody);
        return repository.save(key);
    }

    public IdempotencyKey failWithError(String idempotencyKey, String lockToken, String errorMessage) {
        log.error("Failing idempotency key {}: {}", idempotencyKey, errorMessage);

        IdempotencyKey key = repository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new IllegalArgumentException("Idempotency key not found: " + idempotencyKey));

        if (key.getLockToken() != null && !key.getLockToken().equals(lockToken)) {
            throw new IllegalStateException("Invalid lock token for idempotency key: " + idempotencyKey);
        }

        key.fail(errorMessage);
        return repository.save(key);
    }

    public IdempotencyKey releaseLock(String idempotencyKey, String lockToken) {
        log.info("Releasing lock for idempotency key: {}", idempotencyKey);

        IdempotencyKey key = repository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new IllegalArgumentException("Idempotency key not found: " + idempotencyKey));

        key.releaseLock(lockToken);
        return repository.save(key);
    }

    public Optional<IdempotencyKey> getIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey);
    }

    public boolean exists(String idempotencyKey) {
        return repository.existsByIdempotencyKey(idempotencyKey);
    }

    public IdempotencyKey getIdempotencyKeyOrThrow(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new IllegalArgumentException("Idempotency key not found: " + idempotencyKey));
    }
}
