package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import com.payment.gateway.application.reconciliation.port.out.ReconciliationBatchPort;
import com.payment.gateway.domain.reconciliation.model.ReconciliationBatch;
import com.payment.gateway.domain.reconciliation.model.ReconciliationStatus;
import com.payment.gateway.domain.reconciliation.port.ReconciliationBatchRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReconciliationPersistenceAdapter implements ReconciliationBatchPort, ReconciliationBatchRepositoryPort {

    private final ReconciliationJpaRepository reconciliationJpaRepository;
    private final ReconciliationMapper reconciliationMapper;

    @Override
    public ReconciliationBatch saveBatch(ReconciliationBatch batch) {
        var entity = reconciliationMapper.toEntity(batch);
        var saved = reconciliationJpaRepository.save(entity);
        return reconciliationMapper.toDomain(saved);
    }

    @Override
    public Optional<ReconciliationBatch> findById(String id) {
        return reconciliationJpaRepository.findById(id).map(reconciliationMapper::toDomain);
    }

    @Override
    public ReconciliationBatch save(ReconciliationBatch batch) {
        return saveBatch(batch);
    }

    @Override
    public List<ReconciliationBatch> findByMerchantId(String merchantId) {
        return reconciliationJpaRepository.findByMerchantId(merchantId).stream()
                .map(reconciliationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReconciliationBatch> findByStatus(ReconciliationStatus status) {
        return reconciliationJpaRepository.findByStatus(status.name()).stream()
                .map(reconciliationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReconciliationBatch> findByReconciliationDate(LocalDate date) {
        return reconciliationJpaRepository.findByReconciliationDate(date).stream()
                .map(reconciliationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReconciliationBatch> findByMerchantIdAndGatewayNameAndDate(String merchantId, String gatewayName, LocalDate date) {
        return reconciliationJpaRepository.findByMerchantIdAndGatewayNameAndReconciliationDate(merchantId, gatewayName, date)
                .map(reconciliationMapper::toDomain);
    }

    @Override
    public void deleteById(String id) {
        reconciliationJpaRepository.deleteById(id);
    }
}
