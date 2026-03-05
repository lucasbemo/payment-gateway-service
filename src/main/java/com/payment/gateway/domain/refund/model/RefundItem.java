package com.payment.gateway.domain.refund.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * RefundItem value object representing a line item in a refund.
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefundItem {
    private String id;
    private String paymentItemId;
    private Integer quantity;
    private String reason;

    private RefundItem(Builder builder) {
        this.id = builder.id;
        this.paymentItemId = builder.paymentItemId;
        this.quantity = builder.quantity;
        this.reason = builder.reason;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static RefundItem create(String paymentItemId, Integer quantity, String reason) {
        return new Builder()
                .id(UUID.randomUUID().toString())
                .paymentItemId(paymentItemId)
                .quantity(quantity)
                .reason(reason)
                .build();
    }

    public static class Builder {
        private String id;
        private String paymentItemId;
        private Integer quantity;
        private String reason;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder paymentItemId(String paymentItemId) {
            this.paymentItemId = paymentItemId;
            return this;
        }

        public Builder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public RefundItem build() {
            return new RefundItem(this);
        }
    }
}
