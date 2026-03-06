package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementReportJpaRepository extends JpaRepository<SettlementReportJpaEntity, String> {

    Optional<SettlementReportJpaEntity> findByGatewayReportId(String gatewayReportId);
}
