package com.payment.gateway.application.reconciliation.service;

import com.payment.gateway.application.reconciliation.dto.SettlementReportDTO;
import com.payment.gateway.application.reconciliation.port.in.GenerateSettlementReportUseCase;
import com.payment.gateway.application.reconciliation.port.out.ReportGeneratorPort;
import com.payment.gateway.application.reconciliation.port.out.SettlementReportPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.reconciliation.model.SettlementReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Application service for generating settlement reports.
 */
@Slf4j
@Service
@Transactional
public class GenerateSettlementReportService implements GenerateSettlementReportUseCase {

    private final ReportGeneratorPort reportGeneratorPort;
    private final SettlementReportPort settlementReportPort;

    public GenerateSettlementReportService(ReportGeneratorPort reportGeneratorPort,
                                           SettlementReportPort settlementReportPort) {
        this.reportGeneratorPort = reportGeneratorPort;
        this.settlementReportPort = settlementReportPort;
    }

    @Override
    public SettlementReportDTO generateSettlementReport(String merchantId, String startDate, String endDate, String format) {
        log.info("Generating settlement report for merchant: {} from {} to {} in {} format",
                merchantId, startDate, endDate, format);

        // Validate date range
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        if (start.isAfter(end)) {
            throw new BusinessException("Start date must be before end date");
        }

        // Generate report file
        String filePath = reportGeneratorPort.generateReport(merchantId, startDate, endDate, format);

        // Create settlement report record (using builder since create requires amounts)
        SettlementReport report = SettlementReport.builder()
                .merchantId(merchantId)
                .gatewayName("DEFAULT_GATEWAY")
                .settlementDate(end)
                .currency("USD")
                .filePath(filePath)
                
                .status("GENERATED")
                .build();

        SettlementReport savedReport = settlementReportPort.saveReport(report);

        log.info("Settlement report generated: {}", savedReport.getId());

        return mapToResponse(savedReport);
    }

    private SettlementReportDTO mapToResponse(SettlementReport report) {
        return SettlementReportDTO.builder()
                .id(report.getId())
                .merchantId(report.getMerchantId())
                .gatewayName(report.getGatewayName())
                .settlementDate(report.getSettlementDate().toString())
                .gatewayReportId(report.getGatewayReportId())
                .grossAmount(report.getGrossAmount() != null ? report.getGrossAmount().getAmountInCents() : null)
                .feeAmount(report.getFeeAmount() != null ? report.getFeeAmount().getAmountInCents() : null)
                .netAmount(report.getNetAmount() != null ? report.getNetAmount().getAmountInCents() : null)
                .currency(report.getCurrency())
                .transactionCount(report.getTransactionCount())
                .filePath(report.getFilePath())
                
                .createdAt(report.getCreatedAt())
                .build();
    }
}
