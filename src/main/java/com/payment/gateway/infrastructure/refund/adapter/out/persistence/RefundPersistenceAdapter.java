package com.payment.gateway.infrastructure.refund.adapter.out.persistence;

import com.payment.gateway.application.refund.port.out.RefundQueryPort;
import com.payment.gateway.domain.refund.model.Refund;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefundPersistenceAdapter implements RefundQueryPort {

    private final RefundJpaRepository refundJpaRepository;
    private final RefundMapper refundMapper;

    @Override
    public Refund saveRefund(Refund refund) {
        var entity = refundMapper.toEntity(refund);
        var saved = refundJpaRepository.save(entity);
        return refundMapper.toDomain(saved);
    }

    @Override
    public Optional<Refund> findById(String id) {
        return refundJpaRepository.findById(id).map(refundMapper::toDomain);
    }

    @Override
    public Optional<Refund> findByIdempotencyKey(String refundIdempotencyKey) {
        return refundJpaRepository.findByRefundIdempotencyKey(refundIdempotencyKey).map(refundMapper::toDomain);
    }

    @Override
    public boolean existsByIdempotencyKey(String refundIdempotencyKey) {
        return refundJpaRepository.existsByRefundIdempotencyKey(refundIdempotencyKey);
    }
}
