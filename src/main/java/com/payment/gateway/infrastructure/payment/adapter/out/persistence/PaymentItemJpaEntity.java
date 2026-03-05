package com.payment.gateway.infrastructure.payment.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA Entity for Payment Items.
 */
@Entity
@Table(name = "payment_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentJpaEntity payment;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total", nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    // Builder-style constructor
    private PaymentItemJpaEntity(Builder builder) {
        this.payment = builder.payment;
        this.description = builder.description;
        this.quantity = builder.quantity;
        this.unitPrice = builder.unitPrice;
        this.total = builder.total;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private PaymentJpaEntity payment;
        private String description;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal total;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder payment(PaymentJpaEntity payment) {
            this.payment = payment;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder unitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public Builder total(BigDecimal total) {
            this.total = total;
            return this;
        }

        public PaymentItemJpaEntity build() {
            return new PaymentItemJpaEntity(this);
        }
    }
}
