package com.payment.gateway.infrastructure.refund.adapter.in.rest;

import com.payment.gateway.application.refund.dto.RefundResponse;
import com.payment.gateway.application.refund.port.in.CancelRefundUseCase;
import com.payment.gateway.application.refund.port.in.GetRefundUseCase;
import com.payment.gateway.application.refund.port.in.ProcessRefundUseCase;
import com.payment.gateway.infrastructure.commons.rest.ApiResponse;
import com.payment.gateway.infrastructure.docs.RefundApi;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
public class RefundController implements RefundApi {

    private final ProcessRefundUseCase processRefundUseCase;
    private final GetRefundUseCase getRefundUseCase;
    private final CancelRefundUseCase cancelRefundUseCase;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @Valid @RequestBody CreateRefundRequest request) {
        log.info("Processing refund for payment: {} amount: {}", request.getPaymentId(), request.getAmount());
        var response = processRefundUseCase.processRefund(
                request.getPaymentId(),
                request.getMerchantId(),
                request.getAmount(),
                request.getIdempotencyKey(),
                request.getReason()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Refund processed successfully", response));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RefundResponse>> getRefund(
            @PathVariable String id,
            @RequestParam String merchantId) {
        log.info("Getting refund: {} for merchant: {}", id, merchantId);
        var response = getRefundUseCase.getRefundById(id, merchantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<RefundResponse>> cancelRefund(
            @PathVariable String id,
            @RequestParam String merchantId,
            @RequestParam(required = false) String reason) {
        log.info("Cancelling refund: {} for merchant: {}", id, merchantId);
        var response = cancelRefundUseCase.cancelRefund(id, merchantId, reason);
        return ResponseEntity.ok(ApiResponse.success("Refund cancelled successfully", response));
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Request to process a refund")
    public static class CreateRefundRequest {

        @Schema(description = "Payment ID to refund", example = "pay_abc123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Payment ID is required")
        private String paymentId;

        @Schema(description = "Merchant ID", example = "merch_abc123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Merchant ID is required")
        private String merchantId;

        @Schema(description = "Refund amount in cents", example = "5000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        private Long amount;

        @Schema(description = "Idempotency key for duplicate protection", example = "550e8400-e29b-41d4-a716-446655440000")
        private String idempotencyKey;

        @Schema(description = "Reason for the refund", example = "Customer requested")
        private String reason;
    }
}