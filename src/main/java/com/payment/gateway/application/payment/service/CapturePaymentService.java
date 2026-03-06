package com.payment.gateway.application.payment.service;

import com.payment.gateway.application.payment.dto.PaymentResponse;
import com.payment.gateway.application.payment.port.in.CapturePaymentUseCase;
import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort;
import com.payment.gateway.application.payment.port.out.PaymentQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.payment.model.Payment;
import com.payment.gateway.domain.payment.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application service for capturing authorized payments.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CapturePaymentService implements CapturePaymentUseCase {

    private final PaymentQueryPort paymentQueryPort;
    private final ExternalPaymentProviderPort externalPaymentProviderPort;

    @Override
    public PaymentResponse capturePayment(String paymentId, String merchantId) {
        log.info("Capturing payment: {} for merchant: {}", paymentId, merchantId);

        Payment payment = paymentQueryPort.findById(paymentId)
                .orElseThrow(() -> new BusinessException("Payment not found: " + paymentId));

        // Validate ownership
        if (!payment.getMerchantId().equals(merchantId)) {
            throw new BusinessException("Payment does not belong to merchant: " + merchantId);
        }

        // Validate payment is authorized and can be captured
        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new BusinessException("Payment must be authorized before capture. Current status: " + payment.getStatus());
        }

        // Capture with external provider
        captureWithProvider(payment);

        // Update payment status
        payment.capture();
        Payment savedPayment = paymentQueryPort.savePayment(payment);

        log.info("Payment captured successfully: {}", paymentId);
        return mapToResponse(savedPayment);
    }

    private void captureWithProvider(Payment payment) {
        log.debug("Capturing payment {} with external provider", payment.getId());

        ExternalPaymentProviderPort.PaymentProviderRequest request =
                new ExternalPaymentProviderPort.PaymentProviderRequest(
                        payment.getId(),
                        payment.getMerchantId(),
                        payment.getAmount().getAmountInCents(),
                        payment.getCurrency(),
                        payment.getPaymentMethodId()
                );

        try {
            ExternalPaymentProviderPort.PaymentProviderResult result =
                    externalPaymentProviderPort.capture(request).join();

            if (!result.success()) {
                log.error("Payment capture failed: {} - {}", result.errorCode(), result.errorMessage());
                throw new BusinessException("Payment capture failed: " + result.errorMessage());
            }
        } catch (Exception e) {
            log.error("Payment capture failed with exception: {}", e.getMessage());
            throw new BusinessException("Payment capture failed: " + e.getMessage());
        }
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .merchantId(payment.getMerchantId())
                .customerId(payment.getCustomerId())
                .paymentMethodId(payment.getPaymentMethodId())
                .amount(payment.getAmount().getAmountInCents())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .idempotencyKey(payment.getIdempotencyKey())
                .description(payment.getDescription())
                .items(payment.getItems() != null ? payment.getItems().stream()
                        .map(item -> PaymentResponse.PaymentItemResponse.builder()
                                .description(item.getDescription())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice().getAmountInCents())
                                .total(item.getTotal().getAmountInCents())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
