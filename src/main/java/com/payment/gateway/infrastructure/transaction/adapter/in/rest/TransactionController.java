package com.payment.gateway.infrastructure.transaction.adapter.in.rest;

import com.payment.gateway.application.transaction.dto.TransactionResponse;
import com.payment.gateway.application.transaction.port.in.CaptureTransactionUseCase;
import com.payment.gateway.application.transaction.port.in.GetTransactionUseCase;
import com.payment.gateway.application.transaction.port.in.VoidTransactionUseCase;
import com.payment.gateway.infrastructure.commons.rest.ApiResponse;
import com.payment.gateway.infrastructure.docs.TransactionApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController implements TransactionApi {

    private final GetTransactionUseCase getTransactionUseCase;
    private final CaptureTransactionUseCase captureTransactionUseCase;
    private final VoidTransactionUseCase voidTransactionUseCase;

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @PathVariable String id,
            @RequestParam String merchantId) {
        log.info("Getting transaction: {} for merchant: {}", id, merchantId);
        var response = getTransactionUseCase.getTransactionById(id, merchantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @PostMapping("/{id}/capture")
    public ResponseEntity<ApiResponse<TransactionResponse>> captureTransaction(
            @PathVariable String id,
            @RequestParam String merchantId) {
        log.info("Capturing transaction: {} for merchant: {}", id, merchantId);
        var response = captureTransactionUseCase.captureTransaction(id, merchantId);
        return ResponseEntity.ok(ApiResponse.success("Transaction captured successfully", response));
    }

    @Override
    @PostMapping("/{id}/void")
    public ResponseEntity<ApiResponse<TransactionResponse>> voidTransaction(
            @PathVariable String id,
            @RequestParam String merchantId) {
        log.info("Voiding transaction: {} for merchant: {}", id, merchantId);
        var response = voidTransactionUseCase.voidTransaction(id, merchantId);
        return ResponseEntity.ok(ApiResponse.success("Transaction voided successfully", response));
    }
}