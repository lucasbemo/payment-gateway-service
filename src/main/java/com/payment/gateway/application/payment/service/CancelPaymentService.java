package com.payment.gateway.application.payment.service;

import com.payment.gateway.application.payment.dto.PaymentResponse;
import com.payment.gateway.application.payment.port.in.CancelPaymentUseCase;
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
 * Application service for canceling payments.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CancelPaymentService implements CancelPaymentUseCase {

    private final PaymentQueryPort paymentQueryPort;
    private final ExternalPaymentProviderPort externalPaymentProviderPort;

    @Override
    public PaymentResponse cancelPayment(String paymentId, String merchantId) {
        log.info("Canceling payment: {} for merchant: {}", paymentId, merchantId);

        Payment payment = paymentQueryPort.findById(paymentId)
                .orElseThrow(() -> new BusinessException("Payment not found: " + paymentId));

        // Validate ownership
        if (!payment.getMerchantId().equals(merchantId)) {
            throw new BusinessException("Payment does not belong to merchant: " + merchantId);
        }

        // Validate payment can be canceled
        if (payment.getStatus().isTerminal()) {
            throw new BusinessException("Cannot cancel payment in terminal state: " + payment.getStatus());
        }

        // Cancel with external provider
        cancelWithProvider(payment);

        // Update payment status
        payment.cancel();
        Payment savedPayment = paymentQueryPort.savePayment(payment);

        log.info("Payment canceled successfully: {}", paymentId);
        return mapToResponse(savedPayment);
    }

    private void cancelWithProvider(Payment payment) {
        log.debug("Canceling payment {} with external provider", payment.getId());

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
                    externalPaymentProviderPort.cancel(request).join();

            if (!result.success()) {
                log.warn("Payment cancellation with provider failed: {} - {}",
                        result.errorCode(), result.errorMessage());
                // Don't throw exception - allow local cancellation even if provider fails
            }
        } catch (Exception e) {
            log.warn("Payment cancellation with provider failed with exception: {}", e.getMessage());
            // Don't throw exception - allow local cancellation even if provider fails
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
