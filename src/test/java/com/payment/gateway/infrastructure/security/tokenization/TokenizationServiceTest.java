package com.payment.gateway.infrastructure.security.tokenization;

import com.payment.gateway.commons.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for TokenizationService.
 */
@DisplayName("TokenizationService Tests")
class TokenizationServiceTest {

    private TokenizationService tokenizationService;

    @BeforeEach
    void setUp() {
        tokenizationService = new TokenizationService();
    }

    @Nested
    @DisplayName("Tokenization Tests")
    class TokenizationTests {

        @Test
        @DisplayName("Should tokenize valid card number")
        void shouldTokenizeValidCardNumber() {
            // Given
            String cardNumber = "4532015112830366";

            // When
            String token = tokenizationService.tokenize(cardNumber);

            // Then
            assertNotNull(token);
            assertTrue(token.startsWith("tok_"));
            assertEquals(20, token.length()); // "tok_" + 16 chars
        }

        @Test
        @DisplayName("Should return same token for same card number")
        void shouldReturnSameTokenForSameCardNumber() {
            // Given
            String cardNumber = "4532015112830366";

            // When
            String token1 = tokenizationService.tokenize(cardNumber);
            String token2 = tokenizationService.tokenize(cardNumber);

            // Then
            assertEquals(token1, token2);
        }

        @Test
        @DisplayName("Should tokenize card number with spaces")
        void shouldTokenizeCardNumberWithSpaces() {
            // Given
            String cardNumber = "4532 0151 1283 0366";

            // When
            String token = tokenizationService.tokenize(cardNumber);

            // Then
            assertNotNull(token);
            assertTrue(token.startsWith("tok_"));
        }

        @Test
        @DisplayName("Should tokenize card number with dashes")
        void shouldTokenizeCardNumberWithDashes() {
            // Given
            String cardNumber = "4532-0151-1283-0366";

            // When
            String token = tokenizationService.tokenize(cardNumber);

            // Then
            assertNotNull(token);
            assertTrue(token.startsWith("tok_"));
        }

        @Test
        @DisplayName("Should throw exception for null card number")
        void shouldThrowExceptionForNullCardNumber() {
            // When & Then
            DomainException exception = assertThrows(
                DomainException.class,
                () -> tokenizationService.tokenize(null)
            );
            assertEquals("Card number cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for empty card number")
        void shouldThrowExceptionForEmptyCardNumber() {
            // When & Then
            DomainException exception = assertThrows(
                DomainException.class,
                () -> tokenizationService.tokenize("")
            );
            assertEquals("Card number cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid card number (fails Luhn)")
        void shouldThrowExceptionForInvalidCardNumber() {
            // Given
            String invalidCardNumber = "1234567890123456";

            // When & Then
            DomainException exception = assertThrows(
                DomainException.class,
                () -> tokenizationService.tokenize(invalidCardNumber)
            );
            assertEquals("Invalid card number format", exception.getMessage());
        }

        @Test
        @DisplayName("Should tokenize different card brands")
        void shouldTokenizeDifferentCardBrands() {
            // Given
            String visa = "4532015112830366";
            String mastercard = "5425233430109903";
            String amex = "374245455400126";
            String discover = "6011000990139424";

            // When
            String visaToken = tokenizationService.tokenize(visa);
            String mcToken = tokenizationService.tokenize(mastercard);
            String amexToken = tokenizationService.tokenize(amex);
            String discoverToken = tokenizationService.tokenize(discover);

            // Then
            assertNotNull(visaToken);
            assertNotNull(mcToken);
            assertNotNull(amexToken);
            assertNotNull(discoverToken);
            assertNotEquals(visaToken, mcToken);
            assertNotEquals(amexToken, discoverToken);
        }
    }

    @Nested
    @DisplayName("Detokenization Tests")
    class DetokenizationTests {

        @Test
        @DisplayName("Should detokenize with valid authorization")
        void shouldDetokenizeWithValidAuthorization() {
            // Given
            String cardNumber = "4532015112830366";
            String token = tokenizationService.tokenize(cardNumber);
            String authContext = "PAYMENT_PROCESSOR";

            // When
            String detokenized = tokenizationService.detokenize(token, authContext);

            // Then
            assertEquals(cardNumber, detokenized);
        }

        @Test
        @DisplayName("Should throw exception for unauthorized detokenization")
        void shouldThrowExceptionForUnauthorizedDetokenization() {
            // Given
            String cardNumber = "4532015112830366";
            String token = tokenizationService.tokenize(cardNumber);
            String invalidAuthContext = "UNAUTHORIZED_USER";

            // When & Then
            DomainException exception = assertThrows(
                DomainException.class,
                () -> tokenizationService.detokenize(token, invalidAuthContext)
            );
            assertEquals("Unauthorized: insufficient permissions for detokenization", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null token")
        void shouldThrowExceptionForNullToken() {
            // When & Then
            DomainException exception = assertThrows(
                DomainException.class,
                () -> tokenizationService.detokenize(null, "ADMIN")
            );
            assertEquals("Token cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid token")
        void shouldThrowExceptionForInvalidToken() {
            // When & Then
            DomainException exception = assertThrows(
                DomainException.class,
                () -> tokenizationService.detokenize("invalid_token", "ADMIN")
            );
            assertEquals("Invalid token: token not found in vault", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for deactivated token")
        void shouldThrowExceptionForDeactivationToken() {
            // Given
            String cardNumber = "4532015112830366";
            String token = tokenizationService.tokenize(cardNumber);
            tokenizationService.deactivateToken(token);

            // When & Then
            DomainException exception = assertThrows(
                DomainException.class,
                () -> tokenizationService.detokenize(token, "ADMIN")
            );
            assertEquals("Token is no longer active", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Token Metadata Tests")
    class TokenMetadataTests {

        @Test
        @DisplayName("Should return token metadata")
        void shouldReturnTokenMetadata() {
            // Given
            String cardNumber = "4532015112830366";
            String token = tokenizationService.tokenize(cardNumber);

            // When
            TokenMetadata metadata = tokenizationService.getTokenMetadata(token);

            // Then
            assertNotNull(metadata);
            assertEquals(token, metadata.token());
            assertEquals("0366", metadata.lastFourDigits());
            assertEquals("VISA", metadata.cardBrand());
            assertTrue(metadata.active());
        }

        @Test
        @DisplayName("Should return null for non-existent token")
        void shouldReturnNullForNonExistentToken() {
            // When
            TokenMetadata metadata = tokenizationService.getTokenMetadata("non_existent_token");

            // Then
            assertNull(metadata);
        }

        @Test
        @DisplayName("Should return correct card brand for Mastercard")
        void shouldReturnCorrectCardBrandForMastercard() {
            // Given
            String cardNumber = "5425233430109903";
            String token = tokenizationService.tokenize(cardNumber);

            // When
            TokenMetadata metadata = tokenizationService.getTokenMetadata(token);

            // Then
            assertEquals("MASTERCARD", metadata.cardBrand());
        }

        @Test
        @DisplayName("Should return correct card brand for American Express")
        void shouldReturnCorrectCardBrandForAmex() {
            // Given
            String cardNumber = "374245455400126";
            String token = tokenizationService.tokenize(cardNumber);

            // When
            TokenMetadata metadata = tokenizationService.getTokenMetadata(token);

            // Then
            assertEquals("AMEX", metadata.cardBrand());
        }
    }

    @Nested
    @DisplayName("Token Lifecycle Tests")
    class TokenLifecycleTests {

        @Test
        @DisplayName("Should deactivate token")
        void shouldDeactivateToken() {
            // Given
            String cardNumber = "4532015112830366";
            String token = tokenizationService.tokenize(cardNumber);

            // When
            tokenizationService.deactivateToken(token);

            // Then
            TokenMetadata metadata = tokenizationService.getTokenMetadata(token);
            assertFalse(metadata.active());
        }

        @Test
        @DisplayName("Should reactivate deactivated token")
        void shouldReactivateDeactivationToken() {
            // Given
            String cardNumber = "4532015112830366";
            String token = tokenizationService.tokenize(cardNumber);
            tokenizationService.deactivateToken(token);

            // When
            tokenizationService.reactivateToken(token);

            // Then
            TokenMetadata metadata = tokenizationService.getTokenMetadata(token);
            assertTrue(metadata.active());
        }

        @Test
        @DisplayName("Should return false for invalid token check")
        void shouldReturnFalseForInvalidTokenCheck() {
            // When
            boolean isValid = tokenizationService.isValidToken("non_existent_token");

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should return false for deactivated token check")
        void shouldReturnFalseForDeactivationTokenCheck() {
            // Given
            String cardNumber = "4532015112830366";
            String token = tokenizationService.tokenize(cardNumber);
            tokenizationService.deactivateToken(token);

            // When
            boolean isValid = tokenizationService.isValidToken(token);

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should return true for active token check")
        void shouldReturnTrueForActiveTokenCheck() {
            // Given
            String cardNumber = "4532015112830366";
            String token = tokenizationService.tokenize(cardNumber);

            // When
            boolean isValid = tokenizationService.isValidToken(token);

            // Then
            assertTrue(isValid);
        }
    }
}
