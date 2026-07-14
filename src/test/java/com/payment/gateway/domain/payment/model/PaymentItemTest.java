package com.payment.gateway.domain.payment.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.payment.gateway.commons.model.Money;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PaymentItem Value Object Tests")
class PaymentItemTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("Should build a valid payment item and expose its fields")
        void shouldBuildValidPaymentItem() {
            // Given
            Money unitPrice = Money.of(1500L, USD);
            Money total = Money.of(3000L, USD);

            // When
            PaymentItem item = new PaymentItem("Widget", 2, unitPrice, total);

            // Then
            assertThat(item.getDescription()).isEqualTo("Widget");
            assertThat(item.getQuantity()).isEqualTo(2);
            assertThat(item.getUnitPrice()).isEqualTo(unitPrice);
            assertThat(item.getTotal()).isEqualTo(total);
        }

        @Test
        @DisplayName("Should reject a blank description")
        void shouldRejectBlankDescription() {
            assertThatThrownBy(() -> new PaymentItem("  ", 1, Money.of(100L, USD), Money.of(100L, USD)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Description is required");
        }

        @Test
        @DisplayName("Should reject a non-positive quantity")
        void shouldRejectNonPositiveQuantity() {
            assertThatThrownBy(() -> new PaymentItem("Widget", 0, Money.of(100L, USD), Money.of(100L, USD)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be positive");
        }

        @Test
        @DisplayName("Should reject a null unit price")
        void shouldRejectNullUnitPrice() {
            assertThatThrownBy(() -> new PaymentItem("Widget", 1, null, Money.of(100L, USD)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unit price is required");
        }

        @Test
        @DisplayName("Should reject a null total")
        void shouldRejectNullTotal() {
            assertThatThrownBy(() -> new PaymentItem("Widget", 1, Money.of(100L, USD), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Total is required");
        }
    }

    @Nested
    @DisplayName("Total Validation")
    class TotalValidationTests {

        @Test
        @DisplayName("Should confirm total is valid when it equals quantity * unit price")
        void shouldReturnTrueWhenTotalMatches() {
            // Given
            PaymentItem item = new PaymentItem("Widget", 3, Money.of(1000L, USD), Money.of(3000L, USD));

            // When & Then
            assertThat(item.isTotalValid()).isTrue();
        }

        @Test
        @DisplayName("Should detect an invalid total that does not equal quantity * unit price")
        void shouldReturnFalseWhenTotalDoesNotMatch() {
            // Given
            PaymentItem item = new PaymentItem("Widget", 3, Money.of(1000L, USD), Money.of(2500L, USD));

            // When & Then
            assertThat(item.isTotalValid()).isFalse();
        }
    }
}
