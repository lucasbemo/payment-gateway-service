package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReconciliationJpaRepository extends JpaRepository<ReconciliationBatchJpaEntity, String> {

    List<ReconciliationBatchJpaEntity> findByMerchantId(String merchantId);

    List<ReconciliationBatchJpaEntity> findByStatus(String status);

    List<ReconciliationBatchJpaEntity> findByReconciliationDate(LocalDate reconciliationDate);

    Optional<ReconciliationBatchJpaEntity> findByMerchantIdAndGatewayNameAndReconciliationDate(
            String merchantId, String gatewayName, LocalDate reconciliationDate);
}
