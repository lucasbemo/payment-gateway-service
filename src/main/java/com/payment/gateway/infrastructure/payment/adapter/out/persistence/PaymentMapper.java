package com.payment.gateway.infrastructure.payment.adapter.out.persistence;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.payment.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between Payment domain model and PaymentJpaEntity.
 */
@Component
public class PaymentMapper {

    /**
     * Map domain Payment to JPA entity.
     */
    public PaymentJpaEntity toEntity(Payment payment) {
        long amountInCents = payment.getAmount().getAmountInCents();
        PaymentJpaEntity entity = PaymentJpaEntity.builder()
                .id(payment.getId())
                .merchantId(payment.getMerchantId())
                .customerId(payment.getCustomerId())
                .paymentMethodId(payment.getPaymentMethodId())
                .amount(BigDecimal.valueOf(amountInCents))
                .currency(payment.getCurrency())
                .status(PaymentStatus.valueOf(payment.getStatus().name()))
                .idempotencyKey(payment.getIdempotencyKey())
                .description(payment.getDescription())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();

        // Map items and set the payment reference
        if (payment.getItems() != null) {
            List<PaymentItemJpaEntity> itemEntities = payment.getItems().stream()
                    .map(item -> itemToEntity(item, entity))
                    .collect(Collectors.toList());
            entity.getItems().addAll(itemEntities);
        }

        return entity;
    }

    /**
     * Map PaymentItem domain to JPA entity with payment reference.
     */
    private PaymentItemJpaEntity itemToEntity(PaymentItem item, PaymentJpaEntity payment) {
        long unitPriceCents = item.getUnitPrice().getAmountInCents();
        long totalCents = item.getTotal().getAmountInCents();
        return PaymentItemJpaEntity.builder()
                .payment(payment)
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitPrice(BigDecimal.valueOf(unitPriceCents))
                .total(BigDecimal.valueOf(totalCents))
                .build();
    }

    /**
     * Map PaymentItem domain to JPA entity (without payment reference).
     */
    private PaymentItemJpaEntity itemToEntity(PaymentItem item) {
        return itemToEntity(item, null);
    }

    /**
     * Map JPA entity to domain Payment.
     */
    public Payment toDomain(PaymentJpaEntity entity) {
        Money amount = Money.of(
                entity.getAmount() != null ? entity.getAmount().longValueExact() : 0L,
                Currency.getInstance(entity.getCurrency())
        );

        List<PaymentItem> items = entity.getItems() != null ?
                entity.getItems().stream().map(this::entityToItem).collect(Collectors.toList()) : null;

        Payment payment = Payment.create(
                entity.getMerchantId(),
                amount,
                entity.getCurrency(),
                PaymentMethod.valueOf(entity.getPaymentMethodId() != null ? entity.getPaymentMethodId() : "CREDIT_CARD"),
                entity.getIdempotencyKey(),
                entity.getDescription(),
                PaymentMetadata.empty(),
                items,
                entity.getCustomerId()
        );

        // Set the ID using reflection since Payment doesn't have a setter
        setId(payment, entity.getId());

        // Set the status using reflection since Payment doesn't have a status setter
        setStatus(payment, entity.getStatus());

        return payment;
    }

    /**
     * Set status using reflection since Payment domain object doesn't have a status setter.
     */
    private void setStatus(Payment payment, PaymentStatus status) {
        try {
            java.lang.reflect.Field statusField = Payment.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(payment, com.payment.gateway.domain.payment.model.PaymentStatus.valueOf(status.name()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set payment status", e);
        }
    }

    /**
     * Map PaymentItem JPA entity to domain.
     */
    private PaymentItem entityToItem(PaymentItemJpaEntity entity) {
        Money unitPrice = Money.of(
                entity.getUnitPrice() != null ? entity.getUnitPrice().longValueExact() : 0L,
                Currency.getInstance("USD")
        );
        Money total = Money.of(
                entity.getTotal() != null ? entity.getTotal().longValueExact() : 0L,
                Currency.getInstance("USD")
        );
        return new PaymentItem(entity.getDescription(), entity.getQuantity(), unitPrice, total);
    }

    private void setId(Payment payment, String id) {
        try {
            java.lang.reflect.Field idField = Payment.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(payment, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set payment ID", e);
        }
    }
}
