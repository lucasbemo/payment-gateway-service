package com.payment.gateway.domain.payment.model;

import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.commons.model.Money;
import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Payment aggregate.
 */
class PaymentTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final String MERCHANT_ID = "merchant-123";
    private static final String IDEMPOTENCY_KEY = "idemp-key-123";

    @Test
    void shouldCreatePaymentWithValidData() {
        Money amount = Money.of(10000, USD);

        Payment payment = Payment.create(
            MERCHANT_ID,
            amount,
            "USD",
            PaymentMethod.CREDIT_CARD,
            IDEMPOTENCY_KEY,
            "Test payment",
            PaymentMetadata.empty(),
            null,
            null
        );

        assertNotNull(payment);
        assertEquals(MERCHANT_ID, payment.getMerchantId());
        assertEquals(amount, payment.getAmount());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(IDEMPOTENCY_KEY, payment.getIdempotencyKey());
        assertFalse(payment.isTerminal());
    }

    @Test
    void shouldThrowExceptionWhenMerchantIdIsMissing() {
        Money amount = Money.of(10000, USD);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> Payment.create(
                null,
                amount,
                "USD",
                PaymentMethod.CREDIT_CARD,
                IDEMPOTENCY_KEY,
                "Test payment",
                PaymentMetadata.empty(),
                null,
                null
            )
        );

        assertEquals("Merchant ID is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAmountIsZero() {
        Money amount = Money.zero(USD);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> Payment.create(
                MERCHANT_ID,
                amount,
                "USD",
                PaymentMethod.CREDIT_CARD,
                IDEMPOTENCY_KEY,
                "Test payment",
                PaymentMetadata.empty(),
                null,
                null
            )
        );

        assertEquals("Payment amount must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenIdempotencyKeyIsMissing() {
        Money amount = Money.of(10000, USD);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> Payment.create(
                MERCHANT_ID,
                amount,
                "USD",
                PaymentMethod.CREDIT_CARD,
                null,
                "Test payment",
                PaymentMetadata.empty(),
                null,
                null
            )
        );

        assertEquals("Idempotency key is required", exception.getMessage());
    }

    @Test
    void shouldTransitionFromPendingToAuthorized() {
        Payment payment = createTestPayment();

        payment.authorize();

        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());
    }

    @Test
    void shouldTransitionFromAuthorizedToCaptured() {
        Payment payment = createTestPayment();
        payment.authorize();

        payment.capture();

        assertEquals(PaymentStatus.CAPTURED, payment.getStatus());
        assertTrue(payment.isTerminal());
        assertTrue(payment.isSuccess());
    }

    @Test
    void shouldTransitionFromPendingToFailed() {
        Payment payment = createTestPayment();

        payment.fail();

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertTrue(payment.isTerminal());
        assertFalse(payment.isSuccess());
    }

    @Test
    void shouldTransitionFromPendingToCancelled() {
        Payment payment = createTestPayment();

        payment.cancel();

        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
        assertTrue(payment.isTerminal());
    }

    @Test
    void shouldNotTransitionFromCaptured() {
        Payment payment = createTestPayment();
        payment.authorize();
        payment.capture();

        BusinessException exception = assertThrows(
            BusinessException.class,
            payment::cancel
        );

        assertTrue(exception.getMessage().contains("Cannot transition from CAPTURED to CANCELLED"));
    }

    @Test
    void shouldNotTransitionFromFailed() {
        Payment payment = createTestPayment();
        payment.fail();

        BusinessException exception = assertThrows(
            BusinessException.class,
            payment::authorize
        );

        assertTrue(exception.getMessage().contains("Cannot transition from FAILED to AUTHORIZED"));
    }

    @Test
    void shouldValidateOwnership() {
        Payment payment = createTestPayment();

        assertDoesNotThrow(() -> payment.validateOwnership(MERCHANT_ID));

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> payment.validateOwnership("wrong-merchant-id")
        );

        assertEquals("Payment does not belong to merchant: wrong-merchant-id", exception.getMessage());
    }

    private Payment createTestPayment() {
        return Payment.create(
            MERCHANT_ID,
            Money.of(10000, USD),
            "USD",
            PaymentMethod.CREDIT_CARD,
            IDEMPOTENCY_KEY,
            "Test payment",
            PaymentMetadata.empty(),
            null,
            null
        );
    }
}
