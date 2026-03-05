package com.payment.gateway.application.reconciliation.port.in;

import com.payment.gateway.application.reconciliation.dto.SettlementReportDTO;

/**
 * Use case for generating settlement reports.
 */
public interface GenerateSettlementReportUseCase {

    SettlementReportDTO generateSettlementReport(String merchantId, String startDate, String endDate, String format);
}
