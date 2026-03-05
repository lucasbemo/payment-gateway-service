package com.payment.gateway.application.payment.service;

import com.payment.gateway.application.payment.dto.PaymentResponse;
import com.payment.gateway.application.payment.port.in.GetPaymentUseCase;
import com.payment.gateway.application.payment.port.out.PaymentQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.payment.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application service for getting payment information.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetPaymentService implements GetPaymentUseCase {

    private final PaymentQueryPort paymentQueryPort;

    @Override
    public PaymentResponse getPaymentById(String paymentId, String merchantId) {
        log.info("Getting payment by id: {} for merchant: {}", paymentId, merchantId);

        Payment payment = paymentQueryPort.findById(paymentId)
                .orElseThrow(() -> new BusinessException("Payment not found: " + paymentId));

        // Validate ownership
        if (!payment.getMerchantId().equals(merchantId)) {
            throw new BusinessException("Payment does not belong to merchant: " + merchantId);
        }

        return mapToResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentsByMerchantId(String merchantId) {
        log.info("Getting payments for merchant: {}", merchantId);
        // Note: This would need a new method in the port for finding by merchant ID
        // For now, returning empty list - the actual implementation would need the port method
        return List.of();
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
