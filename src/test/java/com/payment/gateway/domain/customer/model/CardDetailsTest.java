package com.payment.gateway.domain.customer.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CardDetails Value Object Tests")
class CardDetailsTest {

    @Nested
    @DisplayName("Factory Creation & Validation")
    class CreationTests {

        @Test
        @DisplayName("Should create card details with a generated id and provided fields")
        void shouldCreateCardDetails() {
            // Given / When
            CardDetails card = CardDetails.create("4242", "424242", "VISA", 12, 30, "Jane Doe");

            // Then
            assertThat(card.getId()).isNotBlank();
            assertThat(card.getCardNumberLast4()).isEqualTo("4242");
            assertThat(card.getCardNumberBin()).isEqualTo("424242");
            assertThat(card.getCardBrand()).isEqualTo("VISA");
            assertThat(card.getExpiryMonth()).isEqualTo(12);
            assertThat(card.getExpiryYear()).isEqualTo(30);
            assertThat(card.getCardholderName()).isEqualTo("Jane Doe");
        }

        @Test
        @DisplayName("Should reject last-4 that is not exactly four characters")
        void shouldRejectLast4WithWrongLength() {
            assertThatThrownBy(() -> CardDetails.create("123", "424242", "VISA", 12, 30, "Jane"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("exactly 4 digits");
        }

        @Test
        @DisplayName("Should reject last-4 containing non-digits")
        void shouldRejectNonNumericLast4() {
            assertThatThrownBy(() -> CardDetails.create("12ab", "424242", "VISA", 12, 30, "Jane"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("only digits");
        }

        @Test
        @DisplayName("Should reject a BIN shorter than four digits")
        void shouldRejectShortBin() {
            assertThatThrownBy(() -> CardDetails.create("4242", "12", "VISA", 12, 30, "Jane"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("BIN must be at least 4 digits");
        }

        @Test
        @DisplayName("Should reject an out-of-range expiry month")
        void shouldRejectInvalidExpiryMonth() {
            assertThatThrownBy(() -> CardDetails.create("4242", "424242", "VISA", 13, 30, "Jane"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid expiry month");
        }

        @Test
        @DisplayName("Should reject an already-expired two-digit expiry year")
        void shouldRejectExpiredYear() {
            // Given a two-digit year well below the current two-digit year
            assertThatThrownBy(() -> CardDetails.create("4242", "424242", "VISA", 1, 20, "Jane"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Card already expired");
        }
    }

    @Nested
    @DisplayName("Behavior")
    class BehaviorTests {

        @Test
        @DisplayName("Should report a future card as not expired")
        void shouldReportFutureCardNotExpired() {
            CardDetails card =
                    CardDetails.builder().expiryMonth(12).expiryYear(2099).build();

            assertThat(card.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Should report a past card as expired")
        void shouldReportPastCardExpired() {
            CardDetails card =
                    CardDetails.builder().expiryMonth(1).expiryYear(2020).build();

            assertThat(card.isExpired()).isTrue();
        }

        @Test
        @DisplayName("Should default to not being the default payment method and allow marking it")
        void shouldMarkAsDefault() {
            // Given
            CardDetails card = CardDetails.create("4242", "424242", "VISA", 12, 30, "Jane");
            assertThat(card.getIsDefault()).isFalse();

            // When
            card.markAsDefault();

            // Then
            assertThat(card.getIsDefault()).isTrue();
        }

        @Test
        @DisplayName("Should build a masked card number from BIN and last-4")
        void shouldBuildMaskedCardNumber() {
            CardDetails card = CardDetails.create("4242", "424242", "VISA", 12, 30, "Jane");

            assertThat(card.getMaskedCardNumber()).isEqualTo("424242****4242");
        }
    }
}
