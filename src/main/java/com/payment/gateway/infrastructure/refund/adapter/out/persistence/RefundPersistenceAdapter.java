package com.payment.gateway.infrastructure.refund.adapter.out.persistence;

import com.payment.gateway.application.refund.port.out.RefundQueryPort;
import com.payment.gateway.domain.refund.model.Refund;
import com.payment.gateway.domain.refund.model.RefundStatus;
import com.payment.gateway.domain.refund.port.RefundRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RefundPersistenceAdapter implements RefundQueryPort, RefundRepositoryPort {

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

    @Override
    public Refund save(Refund refund) {
        return saveRefund(refund);
    }

    @Override
    public Optional<Refund> findFirstByPaymentId(String paymentId) {
        return refundJpaRepository.findFirstByPaymentId(paymentId).map(refundMapper::toDomain);
    }

    @Override
    public Optional<Refund> findByTransactionId(String transactionId) {
        return refundJpaRepository.findByTransactionId(transactionId).map(refundMapper::toDomain);
    }

    @Override
    public List<Refund> findAllByPaymentId(String paymentId) {
        return refundJpaRepository.findAllByPaymentId(paymentId).stream()
                .map(refundMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Refund> findByMerchantId(String merchantId) {
        return refundJpaRepository.findByMerchantId(merchantId).stream()
                .map(refundMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Refund> findByStatus(RefundStatus status) {
        return refundJpaRepository.findByStatus(status.name()).stream()
                .map(refundMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByRefundIdempotencyKey(String refundIdempotencyKey) {
        return refundJpaRepository.existsByRefundIdempotencyKey(refundIdempotencyKey);
    }

    @Override
    public void deleteById(String id) {
        refundJpaRepository.deleteById(id);
    }
}
