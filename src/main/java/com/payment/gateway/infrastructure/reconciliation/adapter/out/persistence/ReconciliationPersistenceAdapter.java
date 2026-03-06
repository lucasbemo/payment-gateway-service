package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import com.payment.gateway.application.reconciliation.port.out.ReconciliationBatchPort;
import com.payment.gateway.domain.reconciliation.model.ReconciliationBatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReconciliationPersistenceAdapter implements ReconciliationBatchPort {

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
}
