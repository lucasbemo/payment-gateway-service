package com.payment.gateway.infrastructure.payment.adapter.out.persistence;

import com.payment.gateway.domain.payment.model.Payment;
import com.payment.gateway.domain.payment.port.PaymentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Persistence adapter for Payment repository.
 * Implements the domain's PaymentRepositoryPort interface.
 */
@Component
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements PaymentRepositoryPort {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = paymentMapper.toEntity(payment);
        PaymentJpaEntity savedEntity = paymentJpaRepository.save(entity);
        return paymentMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Payment> findById(String id) {
        return paymentJpaRepository.findById(id).map(paymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return paymentJpaRepository.findByIdempotencyKey(idempotencyKey).map(paymentMapper::toDomain);
    }

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return paymentJpaRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public java.util.List<Payment> findByMerchantId(String merchantId) {
        return paymentJpaRepository.findByMerchantId(merchantId)
                .stream()
                .map(paymentMapper::toDomain)
                .collect(java.util.stream.Collectors.toList());
    }
}
