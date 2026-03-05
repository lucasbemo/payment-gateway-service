package com.payment.gateway.domain.payment.model;

import com.payment.gateway.commons.model.Money;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

/**
 * Value Object representing a line item in a payment.
 */
@Getter
@Builder
@EqualsAndHashCode
public class PaymentItem {

    private final String description;
    private final Integer quantity;
    private final Money unitPrice;
    private final Money total;

    public PaymentItem(String description, Integer quantity, Money unitPrice, Money total) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("Unit price is required");
        }
        if (total == null) {
            throw new IllegalArgumentException("Total is required");
        }
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.total = total;
    }

    /**
     * Validate that the total matches quantity * unitPrice.
     */
    public boolean isTotalValid() {
        Money expectedTotal = unitPrice.multiply(java.math.BigDecimal.valueOf(quantity));
        return total.equals(expectedTotal);
    }
}
