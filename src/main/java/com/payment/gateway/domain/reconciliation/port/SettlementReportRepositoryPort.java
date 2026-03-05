package com.payment.gateway.domain.reconciliation.port;

import com.payment.gateway.domain.reconciliation.model.SettlementReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Settlement report repository port interface.
 */
public interface SettlementReportRepositoryPort {
    SettlementReport save(SettlementReport report);
    Optional<SettlementReport> findById(String id);
    List<SettlementReport> findByMerchantId(String merchantId);
    List<SettlementReport> findByGatewayName(String gatewayName);
    Optional<SettlementReport> findByGatewayReportId(String gatewayReportId);
    List<SettlementReport> findBySettlementDate(LocalDate date);
    void deleteById(String id);
}
