package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import com.payment.gateway.application.reconciliation.port.out.SettlementReportPort;
import com.payment.gateway.domain.reconciliation.model.SettlementReport;
import com.payment.gateway.domain.reconciliation.port.SettlementReportRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SettlementReportPersistenceAdapter implements SettlementReportPort, SettlementReportRepositoryPort {

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

    @Override
    public SettlementReport save(SettlementReport report) {
        return saveReport(report);
    }

    @Override
    public List<SettlementReport> findByMerchantId(String merchantId) {
        return settlementReportJpaRepository.findByMerchantId(merchantId).stream()
                .map(settlementReportMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SettlementReport> findByGatewayName(String gatewayName) {
        return settlementReportJpaRepository.findByGatewayName(gatewayName).stream()
                .map(settlementReportMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SettlementReport> findBySettlementDate(LocalDate date) {
        return settlementReportJpaRepository.findBySettlementDate(date).stream()
                .map(settlementReportMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        settlementReportJpaRepository.deleteById(id);
    }
}
