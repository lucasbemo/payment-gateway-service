package com.payment.gateway.application.reconciliation.port.out;

import com.payment.gateway.domain.reconciliation.model.SettlementReport;

import java.util.Optional;

/**
 * Output port for settlement report operations.
 */
public interface SettlementReportPort {

    SettlementReport saveReport(SettlementReport report);

    Optional<SettlementReport> findById(String id);

    Optional<SettlementReport> findByGatewayReportId(String gatewayReportId);
}
