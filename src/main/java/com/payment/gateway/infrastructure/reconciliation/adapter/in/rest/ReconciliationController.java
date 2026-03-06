package com.payment.gateway.infrastructure.reconciliation.adapter.in.rest;

import com.payment.gateway.application.reconciliation.dto.ReconciliationResponse;
import com.payment.gateway.application.reconciliation.dto.SettlementReportDTO;
import com.payment.gateway.application.reconciliation.port.in.GenerateSettlementReportUseCase;
import com.payment.gateway.application.reconciliation.port.in.ReconcileTransactionsUseCase;
import com.payment.gateway.infrastructure.commons.rest.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconcileTransactionsUseCase reconcileTransactionsUseCase;
    private final GenerateSettlementReportUseCase generateSettlementReportUseCase;

    @PostMapping("/reconcile")
    public ResponseEntity<ApiResponse<ReconciliationResponse>> reconcileTransactions(
            @RequestParam String merchantId,
            @RequestParam String date) {
        log.info("Reconciling transactions for merchant: {} date: {}", merchantId, date);
        var response = reconcileTransactionsUseCase.reconcileTransactions(merchantId, date);
        return ResponseEntity.ok(ApiResponse.success("Reconciliation completed", response));
    }

    @PostMapping("/settlement-report")
    public ResponseEntity<ApiResponse<SettlementReportDTO>> generateSettlementReport(
            @RequestParam String merchantId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "JSON") String format) {
        log.info("Generating settlement report for merchant: {} from {} to {}", merchantId, startDate, endDate);
        var response = generateSettlementReportUseCase.generateSettlementReport(merchantId, startDate, endDate, format);
        return ResponseEntity.ok(ApiResponse.success("Settlement report generated", response));
    }
}
