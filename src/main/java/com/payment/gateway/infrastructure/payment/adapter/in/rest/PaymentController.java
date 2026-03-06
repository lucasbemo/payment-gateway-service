package com.payment.gateway.infrastructure.payment.adapter.in.rest;

import com.payment.gateway.application.payment.dto.ProcessPaymentCommand;
import com.payment.gateway.application.payment.port.in.CancelPaymentUseCase;
import com.payment.gateway.application.payment.port.in.CapturePaymentUseCase;
import com.payment.gateway.application.payment.port.in.GetPaymentUseCase;
import com.payment.gateway.application.payment.port.in.ProcessPaymentUseCase;
import com.payment.gateway.infrastructure.commons.rest.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for payment operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final CapturePaymentUseCase capturePaymentUseCase;
    private final CancelPaymentUseCase cancelPaymentUseCase;
    private final GetPaymentUseCase getPaymentUseCase;
    private final PaymentRestMapper paymentRestMapper;

    /**
     * Process a new payment.
     * POST /api/v1/payments
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request) {
        log.info("Processing payment for merchant: {} amount: {} {}",
                request.getMerchantId(), request.getAmountInCents(), request.getCurrency());

        ProcessPaymentCommand command = ProcessPaymentCommand.builder()
                .merchantId(request.getMerchantId())
                .amount(request.getAmountInCents())
                .currency(request.getCurrency())
                .paymentMethodType("CREDIT_CARD")
                .idempotencyKey(idempotencyKey)
                .description(request.getDescription())
                .customerId(request.getCustomerId())
                .items(request.getItems() != null ?
                        request.getItems().stream()
                                .map(item -> ProcessPaymentCommand.PaymentItemDto.builder()
                                        .description(item.getDescription())
                                        .quantity(item.getQuantity())
                                        .unitPrice(item.getUnitPriceInCents())
                                        .build())
                                .collect(Collectors.toList()) : null)
                .build();

        com.payment.gateway.application.payment.dto.PaymentResponse response =
                processPaymentUseCase.processPayment(command);

        PaymentResponse paymentResponse = paymentRestMapper.toResponse(
                com.payment.gateway.domain.payment.model.Payment.create(
                        response.getMerchantId(),
                        com.payment.gateway.commons.model.Money.of(response.getAmount(),
                                java.util.Currency.getInstance(response.getCurrency())),
                        response.getCurrency(),
                        com.payment.gateway.domain.payment.model.PaymentMethod.CREDIT_CARD,
                        response.getIdempotencyKey(),
                        response.getDescription(),
                        com.payment.gateway.domain.payment.model.PaymentMetadata.empty(),
                        null,
                        response.getCustomerId()
                )
        );

        // Set the ID from response
        setId(paymentResponse, response.getId());

        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", paymentResponse));
    }

    /**
     * Get payment by ID.
     * GET /api/v1/payments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable String id,
            @RequestParam String merchantId) {
        log.info("Getting payment: {} for merchant: {}", id, merchantId);

        com.payment.gateway.application.payment.dto.PaymentResponse response =
                getPaymentUseCase.getPaymentById(id, merchantId);

        PaymentResponse paymentResponse = paymentRestMapper.toResponse(
                com.payment.gateway.domain.payment.model.Payment.create(
                        response.getMerchantId(),
                        com.payment.gateway.commons.model.Money.of(response.getAmount(),
                                java.util.Currency.getInstance(response.getCurrency())),
                        response.getCurrency(),
                        com.payment.gateway.domain.payment.model.PaymentMethod.CREDIT_CARD,
                        response.getIdempotencyKey(),
                        response.getDescription(),
                        com.payment.gateway.domain.payment.model.PaymentMetadata.empty(),
                        null,
                        response.getCustomerId()
                )
        );

        setId(paymentResponse, response.getId());

        return ResponseEntity.ok(ApiResponse.success(paymentResponse));
    }

    /**
     * Get all payments for a merchant.
     * GET /api/v1/payments?merchantId={merchantId}
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPayments(
            @RequestParam String merchantId) {
        log.info("Getting payments for merchant: {}", merchantId);

        List<com.payment.gateway.application.payment.dto.PaymentResponse> responses =
                getPaymentUseCase.getPaymentsByMerchantId(merchantId);

        List<PaymentResponse> paymentResponses = responses.stream()
                .map(response -> {
                    PaymentResponse paymentResponse = paymentRestMapper.toResponse(
                            com.payment.gateway.domain.payment.model.Payment.create(
                                    response.getMerchantId(),
                                    com.payment.gateway.commons.model.Money.of(response.getAmount(),
                                            java.util.Currency.getInstance(response.getCurrency())),
                                    response.getCurrency(),
                                    com.payment.gateway.domain.payment.model.PaymentMethod.CREDIT_CARD,
                                    response.getIdempotencyKey(),
                                    response.getDescription(),
                                    com.payment.gateway.domain.payment.model.PaymentMetadata.empty(),
                                    null,
                                    response.getCustomerId()
                            )
                    );
                    setId(paymentResponse, response.getId());
                    return paymentResponse;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(paymentResponses));
    }

    /**
     * Capture an authorized payment.
     * POST /api/v1/payments/{id}/capture
     */
    @PostMapping("/{id}/capture")
    public ResponseEntity<ApiResponse<com.payment.gateway.application.payment.dto.PaymentResponse>> capturePayment(
            @PathVariable String id,
            @RequestParam String merchantId) {
        log.info("Capturing payment: {} for merchant: {}", id, merchantId);
        var response = capturePaymentUseCase.capturePayment(id, merchantId);
        return ResponseEntity.ok(ApiResponse.success("Payment captured successfully", response));
    }

    /**
     * Cancel a payment.
     * POST /api/v1/payments/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<com.payment.gateway.application.payment.dto.PaymentResponse>> cancelPayment(
            @PathVariable String id,
            @RequestParam String merchantId) {
        log.info("Cancelling payment: {} for merchant: {}", id, merchantId);
        var response = cancelPaymentUseCase.cancelPayment(id, merchantId);
        return ResponseEntity.ok(ApiResponse.success("Payment cancelled successfully", response));
    }

    // Helper method to set ID using reflection since PaymentResponse is immutable
    private void setId(PaymentResponse response, String id) {
        try {
            java.lang.reflect.Field idField = PaymentResponse.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(response, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set payment response ID", e);
        }
    }
}
