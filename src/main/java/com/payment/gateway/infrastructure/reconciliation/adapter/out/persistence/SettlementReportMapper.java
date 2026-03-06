package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.reconciliation.model.SettlementReport;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;

@Component
public class SettlementReportMapper {

    public SettlementReportJpaEntity toEntity(SettlementReport report) {
        return SettlementReportJpaEntity.builder()
                .id(report.getId())
                .merchantId(report.getMerchantId())
                .gatewayName(report.getGatewayName())
                .settlementDate(report.getSettlementDate())
                .gatewayReportId(report.getGatewayReportId())
                .grossAmount(report.getGrossAmount() != null ? report.getGrossAmount().getAmount() : null)
                .feeAmount(report.getFeeAmount() != null ? report.getFeeAmount().getAmount() : null)
                .netAmount(report.getNetAmount() != null ? report.getNetAmount().getAmount() : null)
                .currency(report.getCurrency())
                .transactionCount(report.getTransactionCount())
                .reportType("SETTLEMENT")
                .reportFormat("JSON")
                .status(report.getStatus())
                .filePath(report.getFilePath())
                .reconciliationBatchId(report.getReconciliationBatchId())
                .settledAt(report.getSettledAt())
                .createdAt(report.getCreatedAt())
                .build();
    }

    public SettlementReport toDomain(SettlementReportJpaEntity entity) {
        Currency currency = entity.getCurrency() != null
                ? Currency.getInstance(entity.getCurrency())
                : Currency.getInstance("USD");

        Money grossAmount = entity.getGrossAmount() != null
                ? Money.of(entity.getGrossAmount(), currency)
                : Money.zero(currency);
        Money feeAmount = entity.getFeeAmount() != null
                ? Money.of(entity.getFeeAmount(), currency)
                : Money.zero(currency);
        Money netAmount = entity.getNetAmount() != null
                ? Money.of(entity.getNetAmount(), currency)
                : Money.zero(currency);

        return SettlementReport.builder()
                .id(entity.getId())
                .merchantId(entity.getMerchantId())
                .gatewayName(entity.getGatewayName())
                .settlementDate(entity.getSettlementDate())
                .gatewayReportId(entity.getGatewayReportId())
                .grossAmount(grossAmount)
                .feeAmount(feeAmount)
                .netAmount(netAmount)
                .currency(entity.getCurrency())
                .transactionCount(entity.getTransactionCount())
                .status(entity.getStatus())
                .filePath(entity.getFilePath())
                .reconciliationBatchId(entity.getReconciliationBatchId())
                .settledAt(entity.getSettledAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getCreatedAt())
                .build();
    }
}
