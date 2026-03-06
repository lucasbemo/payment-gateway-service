package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SettlementReportJpaRepository extends JpaRepository<SettlementReportJpaEntity, String> {

    Optional<SettlementReportJpaEntity> findByGatewayReportId(String gatewayReportId);

    List<SettlementReportJpaEntity> findByMerchantId(String merchantId);

    List<SettlementReportJpaEntity> findByGatewayName(String gatewayName);

    List<SettlementReportJpaEntity> findBySettlementDate(LocalDate settlementDate);
}
