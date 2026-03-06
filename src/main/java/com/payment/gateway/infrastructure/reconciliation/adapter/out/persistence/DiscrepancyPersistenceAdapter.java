package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import com.payment.gateway.domain.reconciliation.model.Discrepancy;
import com.payment.gateway.domain.reconciliation.model.DiscrepancyStatus;
import com.payment.gateway.domain.reconciliation.port.DiscrepancyRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DiscrepancyPersistenceAdapter implements DiscrepancyRepositoryPort {

    private final DiscrepancyJpaRepository discrepancyJpaRepository;
    private final DiscrepancyMapper discrepancyMapper;

    @Override
    public Discrepancy save(Discrepancy discrepancy) {
        var entity = discrepancyMapper.toEntity(discrepancy);
        var saved = discrepancyJpaRepository.save(entity);
        return discrepancyMapper.toDomain(saved);
    }

    @Override
    public Optional<Discrepancy> findById(String id) {
        return discrepancyJpaRepository.findById(id)
                .map(discrepancyMapper::toDomain);
    }

    @Override
    public List<Discrepancy> findByBatchId(String batchId) {
        return discrepancyJpaRepository.findByReconciliationBatchId(batchId)
                .stream()
                .map(discrepancyMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Discrepancy> findByMerchantId(String merchantId) {
        // No merchant_id column in discrepancies table; return empty list
        return Collections.emptyList();
    }

    @Override
    public List<Discrepancy> findByStatus(DiscrepancyStatus status) {
        return discrepancyJpaRepository.findByResolutionStatus(status.name())
                .stream()
                .map(discrepancyMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Discrepancy> findByTransactionId(String transactionId) {
        return discrepancyJpaRepository.findByTransactionId(transactionId)
                .stream()
                .map(discrepancyMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        discrepancyJpaRepository.deleteById(id);
    }
}
