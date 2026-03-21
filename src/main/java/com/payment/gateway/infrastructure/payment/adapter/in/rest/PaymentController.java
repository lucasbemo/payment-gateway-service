package com.payment.gateway.infrastructure.payment.adapter.in.rest;

import com.payment.gateway.application.payment.dto.ProcessPaymentCommand;
import com.payment.gateway.application.payment.port.in.CancelPaymentUseCase;
import com.payment.gateway.application.payment.port.in.CapturePaymentUseCase;
import com.payment.gateway.application.payment.port.in.GetPaymentUseCase;
import com.payment.gateway.application.payment.port.in.ProcessPaymentUseCase;
import com.payment.gateway.infrastructure.commons.rest.ApiResponse;
import com.payment.gateway.infrastructure.docs.PaymentApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final CapturePaymentUseCase capturePaymentUseCase;
    private final CancelPaymentUseCase cancelPaymentUseCase;
    private final GetPaymentUseCase getPaymentUseCase;
    private final PaymentRestMapper paymentRestMapper;

    @Override
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

        PaymentResponse paymentResponse = paymentRestMapper.toResponse(response);
        setId(paymentResponse, response.getId());

        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", paymentResponse));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable String id,
            @RequestParam String merchantId) {
        log.info("Getting payment: {} for merchant: {}", id, merchantId);

        com.payment.gateway.application.payment.dto.PaymentResponse response =
                getPaymentUseCase.getPaymentById(id, merchantId);

        PaymentResponse paymentResponse = paymentRestMapper.toResponse(response);
        setId(paymentResponse, response.getId());

        return ResponseEntity.ok(ApiResponse.success(paymentResponse));
    }

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPayments(
            @RequestParam String merchantId) {
        log.info("Getting payments for merchant: {}", merchantId);

        List<com.payment.gateway.application.payment.dto.PaymentResponse> responses =
                getPaymentUseCase.getPaymentsByMerchantId(merchantId);

        List<PaymentResponse> paymentResponses = responses.stream()
                .map(response -> {
                    PaymentResponse paymentResponse = paymentRestMapper.toResponse(response);
                    setId(paymentResponse, response.getId());
                    return paymentResponse;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(paymentResponses));
    }

    @Override
    @PostMapping("/{id}/capture")
    public ResponseEntity<ApiResponse<PaymentResponse>> capturePayment(
            @PathVariable String id,
            @RequestParam String merchantId) {
        log.info("Capturing payment: {} for merchant: {}", id, merchantId);
        var response = capturePaymentUseCase.capturePayment(id, merchantId);
        PaymentResponse paymentResponse = paymentRestMapper.toResponse(response);
        setId(paymentResponse, response.getId());
        return ResponseEntity.ok(ApiResponse.success("Payment captured successfully", paymentResponse));
    }

    @Override
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @PathVariable String id,
            @RequestParam String merchantId) {
        log.info("Cancelling payment: {} for merchant: {}", id, merchantId);
        var response = cancelPaymentUseCase.cancelPayment(id, merchantId);
        PaymentResponse paymentResponse = paymentRestMapper.toResponse(response);
        setId(paymentResponse, response.getId());
        return ResponseEntity.ok(ApiResponse.success("Payment cancelled successfully", paymentResponse));
    }

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