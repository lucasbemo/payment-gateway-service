package com.payment.gateway.infrastructure.customer.adapter.out.persistence;

import com.payment.gateway.domain.customer.model.CardDetails;
import com.payment.gateway.domain.customer.model.PaymentMethod;
import org.springframework.stereotype.Component;

/**
 * Mapper between PaymentMethod domain model and PaymentMethodJpaEntity.
 */
@Component
public class PaymentMethodMapper {

    public PaymentMethodJpaEntity toEntity(PaymentMethod paymentMethod) {
        PaymentMethodJpaEntity entity = PaymentMethodJpaEntity.builder()
                .id(paymentMethod.getId())
                .customerId(paymentMethod.getCustomerId())
                .merchantId(null) // Will be set by the customer relationship
                .type(PaymentMethodType.valueOf(paymentMethod.getType().name()))
                .token(paymentMethod.getToken())
                .status(PaymentMethodStatus.valueOf(paymentMethod.getStatus().name()))
                .lastFour(paymentMethod.getBankAccountLast4())
                .isDefault(paymentMethod.getIsDefault() != null ? paymentMethod.getIsDefault() : false)
                .createdAt(paymentMethod.getCreatedAt())
                .updatedAt(paymentMethod.getUpdatedAt())
                .build();

        // Map card details if present
        CardDetails cardDetails = paymentMethod.getCardDetails();
        if (cardDetails != null) {
            entity.setExpiryMonth(String.valueOf(cardDetails.getExpiryMonth()));
            entity.setExpiryYear(String.valueOf(cardDetails.getExpiryYear()));
            entity.setBrand(cardDetails.getCardBrand());
            if (entity.getLastFour() == null) {
                entity.setLastFour(cardDetails.getCardNumberLast4());
            }
        }

        return entity;
    }

    public PaymentMethod toDomain(PaymentMethodJpaEntity entity) {
        return PaymentMethod.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .type(com.payment.gateway.domain.customer.model.PaymentMethodType.valueOf(entity.getType().name()))
                .status(com.payment.gateway.domain.customer.model.PaymentMethodStatus.valueOf(entity.getStatus().name()))
                .token(entity.getToken())
                .bankAccountLast4(entity.getLastFour())
                .isDefault(entity.isDefault())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
