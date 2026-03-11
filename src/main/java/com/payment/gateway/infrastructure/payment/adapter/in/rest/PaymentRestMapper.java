package com.payment.gateway.infrastructure.payment.adapter.in.rest;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.payment.model.Payment;
import com.payment.gateway.domain.payment.model.PaymentItem;
import com.payment.gateway.infrastructure.payment.adapter.out.persistence.PaymentItemJpaEntity;
import com.payment.gateway.infrastructure.payment.adapter.out.persistence.PaymentJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between Payment domain model and REST DTOs.
 */
@Component
public class PaymentRestMapper {

    public Payment toDomain(CreatePaymentRequest request) {
        Money amount = Money.of(request.getAmountInCents(), Currency.getInstance(request.getCurrency()));

        List<PaymentItem> items = null;
        if (request.getItems() != null) {
            items = request.getItems().stream()
                    .map(item -> {
                        Money unitPrice = Money.of(item.getUnitPriceInCents(), Currency.getInstance(request.getCurrency()));
                        Money total = Money.of(
                                item.getUnitPriceInCents() * item.getQuantity(),
                                Currency.getInstance(request.getCurrency())
                        );
                        return new PaymentItem(item.getDescription(), item.getQuantity(), unitPrice, total);
                    })
                    .collect(Collectors.toList());
        }

        return Payment.create(
                request.getMerchantId(),
                amount,
                request.getCurrency(),
                com.payment.gateway.domain.payment.model.PaymentMethod.CREDIT_CARD,
                request.getIdempotencyKey(),
                request.getDescription(),
                com.payment.gateway.domain.payment.model.PaymentMetadata.empty(),
                items,
                request.getCustomerId()
        );
    }

    public PaymentResponse toResponse(Payment payment) {
        List<PaymentResponse.PaymentItemResponse> items = null;
        if (payment.getItems() != null) {
            items = payment.getItems().stream()
                    .map(item -> PaymentResponse.PaymentItemResponse.builder()
                            .description(item.getDescription())
                            .quantity(item.getQuantity())
                            .unitPriceInCents(item.getUnitPrice().getAmountInCents())
                            .totalInCents(item.getTotal().getAmountInCents())
                            .build())
                    .collect(Collectors.toList());
        }

        return PaymentResponse.builder()
                .id(payment.getId())
                .merchantId(payment.getMerchantId())
                .customerId(payment.getCustomerId())
                .paymentMethodId(payment.getPaymentMethodId())
                .amountInCents(payment.getAmount().getAmountInCents())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .idempotencyKey(payment.getIdempotencyKey())
                .description(payment.getDescription())
                .items(items)
                .createdAt(payment.getCreatedAt())
                .build();
    }

    public PaymentResponse toResponse(PaymentJpaEntity entity) {
        List<PaymentResponse.PaymentItemResponse> items = null;
        if (entity.getItems() != null) {
            items = entity.getItems().stream()
                    .map(item -> PaymentResponse.PaymentItemResponse.builder()
                            .description(item.getDescription())
                            .quantity(item.getQuantity())
                            .unitPriceInCents(item.getUnitPrice().longValueExact())
                            .totalInCents(item.getTotal().longValueExact())
                            .build())
                    .collect(Collectors.toList());
        }

        return PaymentResponse.builder()
                .id(entity.getId())
                .merchantId(entity.getMerchantId())
                .customerId(entity.getCustomerId())
                .paymentMethodId(entity.getPaymentMethodId())
                .amountInCents(entity.getAmount().longValueExact())
                .currency(entity.getCurrency())
                .status(entity.getStatus().name())
                .idempotencyKey(entity.getIdempotencyKey())
                .description(entity.getDescription())
                .gatewayTransactionId(entity.getGatewayTransactionId())
                .errorCode(entity.getErrorCode())
                .errorMessage(entity.getErrorMessage())
                .items(items)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .authorizedAt(entity.getAuthorizedAt())
                .capturedAt(entity.getCapturedAt())
                .build();
    }

    /**
     * Map application PaymentResponse DTO to REST PaymentResponse DTO.
     */
    public PaymentResponse toResponse(com.payment.gateway.application.payment.dto.PaymentResponse response) {
        List<PaymentResponse.PaymentItemResponse> items = null;
        if (response.getItems() != null) {
            items = response.getItems().stream()
                    .map(item -> PaymentResponse.PaymentItemResponse.builder()
                            .description(item.getDescription())
                            .quantity(item.getQuantity())
                            .unitPriceInCents(item.getUnitPrice())
                            .totalInCents(item.getTotal())
                            .build())
                    .collect(Collectors.toList());
        }

        return PaymentResponse.builder()
                .id(response.getId())
                .merchantId(response.getMerchantId())
                .customerId(response.getCustomerId())
                .paymentMethodId(response.getPaymentMethodId())
                .amountInCents(response.getAmount())
                .currency(response.getCurrency())
                .status(response.getStatus())
                .idempotencyKey(response.getIdempotencyKey())
                .description(response.getDescription())
                .items(items)
                .createdAt(response.getCreatedAt())
                .build();
    }
}
