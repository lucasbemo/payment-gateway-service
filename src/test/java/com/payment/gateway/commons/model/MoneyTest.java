package com.payment.gateway.commons.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Money value object.
 */
class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void shouldCreateMoneyWithValidData() {
        Money money = Money.of(new BigDecimal("100.50"), USD);

        assertEquals(new BigDecimal("100.50"), money.getAmount());
        assertEquals(USD, money.getCurrency());
    }

    @Test
    void shouldCreateMoneyFromCents() {
        Money money = Money.of(10050, USD);

        assertEquals(new BigDecimal("100.50"), money.getAmount());
        assertEquals(USD, money.getCurrency());
        assertEquals(10050L, money.getAmountInCents());
    }

    @Test
    void shouldCreateZeroMoney() {
        Money money = Money.zero(USD);

        assertEquals(BigDecimal.ZERO.setScale(2), money.getAmount());
        assertEquals(USD, money.getCurrency());
        assertTrue(money.isZero());
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Money.of(null, USD)
        );
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNegative() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Money.of(new BigDecimal("-10.00"), USD)
        );
    }

    @Test
    void shouldThrowExceptionWhenCurrencyIsNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Money.of(new BigDecimal("100.00"), null)
        );
    }

    @Test
    void shouldAddMoneyWithSameCurrency() {
        Money money1 = Money.of(10000, USD);
        Money money2 = Money.of(5000, USD);

        Money result = money1.add(money2);

        assertEquals(new BigDecimal("150.00"), result.getAmount());
        assertEquals(USD, result.getCurrency());
    }

    @Test
    void shouldNotAddMoneyWithDifferentCurrency() {
        Money money1 = Money.of(10000, USD);
        Money money2 = Money.of(5000, EUR);

        assertThrows(
            IllegalArgumentException.class,
            () -> money1.add(money2)
        );
    }

    @Test
    void shouldSubtractMoneyWithSameCurrency() {
        Money money1 = Money.of(10000, USD);
        Money money2 = Money.of(3000, USD);

        Money result = money1.subtract(money2);

        assertEquals(new BigDecimal("70.00"), result.getAmount());
        assertEquals(USD, result.getCurrency());
    }

    @Test
    void shouldNotSubtractWhenResultIsNegative() {
        Money money1 = Money.of(5000, USD);
        Money money2 = Money.of(10000, USD);

        assertThrows(
            IllegalArgumentException.class,
            () -> money1.subtract(money2)
        );
    }

    @Test
    void shouldMultiplyMoney() {
        Money money = Money.of(10000, USD);

        Money result = money.multiply(new BigDecimal("2.5"));

        assertEquals(new BigDecimal("250.00"), result.getAmount());
        assertEquals(USD, result.getCurrency());
    }

    @Test
    void shouldNotMultiplyWithNegativeMultiplier() {
        Money money = Money.of(10000, USD);

        assertThrows(
            IllegalArgumentException.class,
            () -> money.multiply(new BigDecimal("-1"))
        );
    }

    @Test
    void shouldCompareMoneyWithSameCurrency() {
        Money money1 = Money.of(10000, USD);
        Money money2 = Money.of(5000, USD);
        Money money3 = Money.of(10000, USD);

        assertTrue(money1.isGreaterThan(money2));
        assertFalse(money2.isGreaterThan(money1));
        assertTrue(money1.isGreaterThanOrEqual(money3));
        assertFalse(money1.isGreaterThan(money3));
    }

    @Test
    void shouldNotCompareMoneyWithDifferentCurrency() {
        Money money1 = Money.of(10000, USD);
        Money money2 = Money.of(5000, EUR);

        assertThrows(
            IllegalArgumentException.class,
            () -> money1.isGreaterThan(money2)
        );
    }

    @Test
    void shouldRespectEqualsAndHashCode() {
        Money money1 = Money.of(10000, USD);
        Money money2 = Money.of(10000, USD);

        assertEquals(money1, money2);
        assertEquals(money1.hashCode(), money2.hashCode());
    }

    @Test
    void shouldToStringProperly() {
        Money money = Money.of(10000, USD);

        String result = money.toString();

        assertTrue(result.contains("USD"));
        assertTrue(result.contains("100.00"));
    }
}
