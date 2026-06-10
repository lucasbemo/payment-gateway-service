package com.payment.gateway.application.reconciliation.service;

import com.payment.gateway.application.reconciliation.dto.SettlementReportDTO;
import com.payment.gateway.application.reconciliation.port.in.GenerateSettlementReportUseCase;
import com.payment.gateway.application.reconciliation.port.out.ReportGeneratorPort;
import com.payment.gateway.application.reconciliation.port.out.SettlementReportPort;
import com.payment.gateway.application.transaction.port.out.TransactionQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.reconciliation.model.SettlementReport;
import com.payment.gateway.domain.transaction.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Application service for generating settlement reports.
 */
@Slf4j
@Service
@Transactional
public class GenerateSettlementReportService implements GenerateSettlementReportUseCase {

    private final ReportGeneratorPort reportGeneratorPort;
    private final SettlementReportPort settlementReportPort;
    private final TransactionQueryPort transactionQueryPort;

    public GenerateSettlementReportService(ReportGeneratorPort reportGeneratorPort,
                                           SettlementReportPort settlementReportPort,
                                           TransactionQueryPort transactionQueryPort) {
        this.reportGeneratorPort = reportGeneratorPort;
        this.settlementReportPort = settlementReportPort;
        this.transactionQueryPort = transactionQueryPort;
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

        // Load the period's transactions (start 00:00 UTC inclusive to end-of-period 24:00 UTC)
        List<Transaction> transactions = transactionQueryPort.findByMerchantIdAndCreatedAtBetween(
                merchantId,
                start.atStartOfDay(ZoneOffset.UTC).toInstant(),
                end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());

        long totalAmount = transactions.stream()
                .filter(t -> t.getAmount() != null)
                .mapToLong(t -> t.getAmount().getAmountInCents())
                .sum();
        int transactionCount = transactions.size();

        // Generate report file
        String filePath = reportGeneratorPort.generateReport(merchantId, startDate, endDate, format);

        // Create settlement report record (using builder since create requires amounts)
        Instant now = Instant.now();
        SettlementReport report = SettlementReport.builder()
                .id(UUID.randomUUID().toString())
                .merchantId(merchantId)
                .gatewayName("DEFAULT_GATEWAY")
                .settlementDate(end)
                .currency("USD")
                .transactionCount(transactionCount)
                .filePath(filePath)
                .status("GENERATED")
                .createdAt(now)
                .updatedAt(now)
                .build();

        SettlementReport savedReport = settlementReportPort.saveReport(report);

        log.info("Settlement report generated: {} ({} transactions, {} cents)",
                savedReport.getId(), transactionCount, totalAmount);

        return mapToResponse(savedReport, totalAmount);
    }

    private SettlementReportDTO mapToResponse(SettlementReport report, Long totalAmount) {
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
                .totalAmount(totalAmount)
                .transactionCount(report.getTransactionCount())
                .filePath(report.getFilePath())

                .createdAt(report.getCreatedAt())
                .build();
    }
}
