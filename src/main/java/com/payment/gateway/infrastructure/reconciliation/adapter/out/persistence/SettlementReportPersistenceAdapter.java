package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import com.payment.gateway.application.reconciliation.port.out.SettlementReportPort;
import com.payment.gateway.domain.reconciliation.model.SettlementReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SettlementReportPersistenceAdapter implements SettlementReportPort {

    private final SettlementReportJpaRepository settlementReportJpaRepository;
    private final SettlementReportMapper settlementReportMapper;

    @Override
    public SettlementReport saveReport(SettlementReport report) {
        var entity = settlementReportMapper.toEntity(report);
        var saved = settlementReportJpaRepository.save(entity);
        return settlementReportMapper.toDomain(saved);
    }

    @Override
    public Optional<SettlementReport> findById(String id) {
        return settlementReportJpaRepository.findById(id).map(settlementReportMapper::toDomain);
    }

    @Override
    public Optional<SettlementReport> findByGatewayReportId(String gatewayReportId) {
        return settlementReportJpaRepository.findByGatewayReportId(gatewayReportId).map(settlementReportMapper::toDomain);
    }
}
