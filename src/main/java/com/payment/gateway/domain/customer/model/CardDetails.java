package com.payment.gateway.domain.customer.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.UUID;

/**
 * CardDetails value object representing card information.
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardDetails {
    private String id;
    private String cardNumberLast4;
    private String cardNumberBin;
    private String cardBrand;
    private Integer expiryMonth;
    private Integer expiryYear;
    private String cardholderName;
    private String fingerprint;
    private String fundingType;
    private String country;
    private Boolean isDefault;

    private CardDetails(Builder builder) {
        this.id = builder.id;
        this.cardNumberLast4 = builder.cardNumberLast4;
        this.cardNumberBin = builder.cardNumberBin;
        this.cardBrand = builder.cardBrand;
        this.expiryMonth = builder.expiryMonth;
        this.expiryYear = builder.expiryYear;
        this.cardholderName = builder.cardholderName;
        this.fingerprint = builder.fingerprint;
        this.fundingType = builder.fundingType;
        this.country = builder.country;
        this.isDefault = builder.isDefault;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static CardDetails create(String cardNumberLast4, String cardNumberBin, String cardBrand,
                                      int expiryMonth, int expiryYear, String cardholderName) {
        validateCardNumberLast4(cardNumberLast4);
        validateCardNumberBin(cardNumberBin);
        validateExpiryDate(expiryMonth, expiryYear);

        return new Builder()
                .id(UUID.randomUUID().toString())
                .cardNumberLast4(cardNumberLast4)
                .cardNumberBin(cardNumberBin)
                .cardBrand(cardBrand)
                .expiryMonth(expiryMonth)
                .expiryYear(expiryYear)
                .cardholderName(cardholderName)
                .build();
    }

    public boolean isExpired() {
        YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
        return expiry.isBefore(YearMonth.now());
    }

    public void markAsDefault() {
        this.isDefault = true;
    }

    public String getMaskedCardNumber() {
        return cardNumberBin + "****" + cardNumberLast4;
    }

    private static void validateCardNumberLast4(String cardNumberLast4) {
        if (cardNumberLast4 == null || cardNumberLast4.length() != 4) {
            throw new IllegalArgumentException("Card number last 4 must be exactly 4 digits");
        }
        if (!cardNumberLast4.matches("\\d{4}")) {
            throw new IllegalArgumentException("Card number last 4 must contain only digits");
        }
    }

    private static void validateCardNumberBin(String cardNumberBin) {
        if (cardNumberBin == null || cardNumberBin.length() < 4) {
            throw new IllegalArgumentException("Card number BIN must be at least 4 digits");
        }
    }

    private static void validateExpiryDate(int expiryMonth, int expiryYear) {
        if (expiryMonth < 1 || expiryMonth > 12) {
            throw new IllegalArgumentException("Invalid expiry month: " + expiryMonth);
        }
        int currentYear = YearMonth.now().getYear() % 100;
        if (expiryYear < currentYear) {
            throw new IllegalArgumentException("Card already expired");
        }
    }

    public static class Builder {
        private String id;
        private String cardNumberLast4;
        private String cardNumberBin;
        private String cardBrand;
        private Integer expiryMonth;
        private Integer expiryYear;
        private String cardholderName;
        private String fingerprint;
        private String fundingType;
        private String country;
        private Boolean isDefault = false;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder cardNumberLast4(String cardNumberLast4) {
            this.cardNumberLast4 = cardNumberLast4;
            return this;
        }

        public Builder cardNumberBin(String cardNumberBin) {
            this.cardNumberBin = cardNumberBin;
            return this;
        }

        public Builder cardBrand(String cardBrand) {
            this.cardBrand = cardBrand;
            return this;
        }

        public Builder expiryMonth(Integer expiryMonth) {
            this.expiryMonth = expiryMonth;
            return this;
        }

        public Builder expiryYear(Integer expiryYear) {
            this.expiryYear = expiryYear;
            return this;
        }

        public Builder cardholderName(String cardholderName) {
            this.cardholderName = cardholderName;
            return this;
        }

        public Builder fingerprint(String fingerprint) {
            this.fingerprint = fingerprint;
            return this;
        }

        public Builder fundingType(String fundingType) {
            this.fundingType = fundingType;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder isDefault(Boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        public CardDetails build() {
            return new CardDetails(this);
        }
    }
}
