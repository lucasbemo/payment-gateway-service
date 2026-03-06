package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReconciliationJpaRepository extends JpaRepository<ReconciliationBatchJpaEntity, String> {
}
