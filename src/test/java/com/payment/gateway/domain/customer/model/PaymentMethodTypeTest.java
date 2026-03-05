package com.payment.gateway.domain.customer.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PaymentMethodType Enum Tests")
class PaymentMethodTypeTest {

    @Nested
    @DisplayName("Enum Values Exist")
    class EnumValuesExistTests {

        @Test
        @DisplayName("CREDIT_CARD type exists")
        void creditCardExists() {
            assertThat(PaymentMethodType.CREDIT_CARD).isNotNull();
        }

        @Test
        @DisplayName("DEBIT_CARD type exists")
        void debitCardExists() {
            assertThat(PaymentMethodType.DEBIT_CARD).isNotNull();
        }

        @Test
        @DisplayName("BANK_ACCOUNT type exists")
        void bankAccountExists() {
            assertThat(PaymentMethodType.BANK_ACCOUNT).isNotNull();
        }

        @Test
        @DisplayName("DIGITAL_WALLET type exists")
        void digitalWalletExists() {
            assertThat(PaymentMethodType.DIGITAL_WALLET).isNotNull();
        }

        @Test
        @DisplayName("CRYPTO type exists")
        void cryptoExists() {
            assertThat(PaymentMethodType.CRYPTO).isNotNull();
        }

        @Test
        @DisplayName("BUY_NOW_PAY_LATER type exists")
        void buyNowPayLaterExists() {
            assertThat(PaymentMethodType.BUY_NOW_PAY_LATER).isNotNull();
        }
    }

    @Nested
    @DisplayName("Value Of Tests")
    class ValueOfTests {

        @Test
        @DisplayName("Should return correct type from valueOf")
        void shouldReturnCorrectTypeFromValueOf() {
            assertThat(PaymentMethodType.valueOf("CREDIT_CARD")).isEqualTo(PaymentMethodType.CREDIT_CARD);
            assertThat(PaymentMethodType.valueOf("DEBIT_CARD")).isEqualTo(PaymentMethodType.DEBIT_CARD);
            assertThat(PaymentMethodType.valueOf("BANK_ACCOUNT")).isEqualTo(PaymentMethodType.BANK_ACCOUNT);
            assertThat(PaymentMethodType.valueOf("DIGITAL_WALLET")).isEqualTo(PaymentMethodType.DIGITAL_WALLET);
            assertThat(PaymentMethodType.valueOf("CRYPTO")).isEqualTo(PaymentMethodType.CRYPTO);
            assertThat(PaymentMethodType.valueOf("BUY_NOW_PAY_LATER")).isEqualTo(PaymentMethodType.BUY_NOW_PAY_LATER);
        }

        @Test
        @DisplayName("Should throw exception for invalid type")
        void shouldThrowExceptionForInvalidType() {
            assertThatThrownBy(() -> PaymentMethodType.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Values Array Tests")
    class ValuesArrayTests {

        @Test
        @DisplayName("Should return all payment method types")
        void shouldReturnAllPaymentMethodTypes() {
            PaymentMethodType[] values = PaymentMethodType.values();
            assertThat(values).hasSize(6);
            assertThat(values).containsExactlyInAnyOrder(
                PaymentMethodType.CREDIT_CARD,
                PaymentMethodType.DEBIT_CARD,
                PaymentMethodType.BANK_ACCOUNT,
                PaymentMethodType.DIGITAL_WALLET,
                PaymentMethodType.CRYPTO,
                PaymentMethodType.BUY_NOW_PAY_LATER
            );
        }
    }
}
