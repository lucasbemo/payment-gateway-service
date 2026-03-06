package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscrepancyJpaRepository extends JpaRepository<DiscrepancyJpaEntity, String> {

    List<DiscrepancyJpaEntity> findByReconciliationBatchId(String batchId);

    List<DiscrepancyJpaEntity> findByResolutionStatus(String status);

    List<DiscrepancyJpaEntity> findByTransactionId(String transactionId);
}
