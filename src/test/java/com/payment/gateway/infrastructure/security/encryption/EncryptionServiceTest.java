package com.payment.gateway.infrastructure.security.encryption;

import com.payment.gateway.commons.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for EncryptionService.
 */
@DisplayName("EncryptionService Tests")
class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService("test-master-key-for-encryption-service");
    }

    @Nested
    @DisplayName("Encryption Tests")
    class EncryptionTests {

        @Test
        @DisplayName("Should encrypt plain text")
        void shouldEncryptPlainText() {
            // Given
            String plainText = "sensitive-card-data-1234567890123456";

            // When
            String encrypted = encryptionService.encrypt(plainText);

            // Then
            assertNotNull(encrypted);
            assertNotEquals(plainText, encrypted);
        }

        @Test
        @DisplayName("Should produce different ciphertext for same plaintext")
        void shouldProduceDifferentCiphertextForSamePlaintext() {
            // Given
            String plainText = "sensitive-data";

            // When
            String encrypted1 = encryptionService.encrypt(plainText);
            String encrypted2 = encryptionService.encrypt(plainText);

            // Then
            assertNotNull(encrypted1);
            assertNotNull(encrypted2);
            assertNotEquals(encrypted1, encrypted2); // GCM uses random IV
        }

        @Test
        @DisplayName("Should throw exception for null plain text")
        void shouldThrowExceptionForNullPlainText() {
            // When & Then
            DomainException exception = assertThrows(
                DomainException.class,
                () -> encryptionService.encrypt(null)
            );
            assertEquals("Plain text cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for empty plain text")
        void shouldThrowExceptionForEmptyPlainText() {
            // When & Then
            DomainException exception = assertThrows(
                DomainException.class,
                () -> encryptionService.encrypt("")
            );
            assertEquals("Plain text cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should encrypt sensitive card number")
        void shouldEncryptSensitiveCardNumber() {
            // Given
            String cardNumber = "4532015112830366";

            // When
            String encrypted = encryptionService.encrypt(cardNumber);

            // Then
            assertNotNull(encrypted);
            assertFalse(encrypted.contains(cardNumber));
        }
    }

    @Nested
    @DisplayName("Decryption Tests")
    class DecryptionTests {

        @Test
        @DisplayName("Should decrypt encrypted data")
        void shouldDecryptEncryptedData() {
            // Given
            String plainText = "sensitive-data-to-encrypt";
            String encrypted = encryptionService.encrypt(plainText);

            // When
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertEquals(plainText, decrypted);
        }

        @Test
        @DisplayName("Should decrypt card number correctly")
        void shouldDecryptCardNumberCorrectly() {
            // Given
            String cardNumber = "4532015112830366";
            String encrypted = encryptionService.encrypt(cardNumber);

            // When
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertEquals(cardNumber, decrypted);
        }

        @Test
        @DisplayName("Should throw exception for null encrypted data")
        void shouldThrowExceptionForNullEncryptedData() {
            // When & Then
            DomainException exception = assertThrows(
                DomainException.class,
                () -> encryptionService.decrypt(null)
            );
            assertEquals("Encrypted data cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for empty encrypted data")
        void shouldThrowExceptionForEmptyEncryptedData() {
            // When & Then
            DomainException exception = assertThrows(
                DomainException.class,
                () -> encryptionService.decrypt("")
            );
            assertEquals("Encrypted data cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid encrypted data")
        void shouldThrowExceptionForInvalidEncryptedData() {
            // When & Then
            assertThrows(
                DomainException.class,
                () -> encryptionService.decrypt("invalid-encrypted-data")
            );
        }
    }

    @Nested
    @DisplayName("Key Rotation Tests")
    class KeyRotationTests {

        @Test
        @DisplayName("Should rotate encryption key")
        void shouldRotateEncryptionKey() {
            // When
            int newVersion = encryptionService.rotateKey();

            // Then
            assertTrue(newVersion > 0);
        }

        @Test
        @DisplayName("Should decrypt old data after key rotation")
        void shouldDecryptOldDataAfterKeyRotation() {
            // Given
            String plainText = "sensitive-data";
            String encrypted = encryptionService.encrypt(plainText);

            // When
            encryptionService.rotateKey();
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertEquals(plainText, decrypted);
        }

        @Test
        @DisplayName("Should re-encrypt with current key")
        void shouldReEncryptWithCurrentKey() {
            // Given
            String plainText = "sensitive-data";
            String oldEncrypted = encryptionService.encrypt(plainText);
            int oldKeyVersion = encryptionService.getCurrentKeyVersion();

            // When
            encryptionService.rotateKey();
            String newEncrypted = encryptionService.reEncryptWithCurrentKey(oldEncrypted);

            // Then
            assertNotNull(newEncrypted);
            assertNotEquals(oldEncrypted, newEncrypted);
            assertEquals(plainText, encryptionService.decrypt(newEncrypted));
        }

        @Test
        @DisplayName("Should track key versions")
        void shouldTrackKeyVersions() {
            // Given
            int initialVersion = encryptionService.getCurrentKeyVersion();

            // When
            int version1 = encryptionService.rotateKey();
            int version2 = encryptionService.rotateKey();

            // Then
            assertTrue(version1 > initialVersion);
            assertTrue(version2 > version1);
        }
    }

    @Nested
    @DisplayName("Round-Trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should encrypt and decrypt various data types")
        void shouldEncryptAndDecryptVariousDataTypes() {
            // Given
            String[] testData = {
                "4532015112830366", // Card number
                "123", // CVV
                "John Doe", // Name
                "2025-12-31", // Expiry
                "sensitive-api-secret-key" // API secret
            };

            // When & Then
            for (String data : testData) {
                String encrypted = encryptionService.encrypt(data);
                String decrypted = encryptionService.decrypt(encrypted);
                assertEquals(data, decrypted, "Failed for: " + data);
            }
        }

        @Test
        @DisplayName("Should handle long text")
        void shouldHandleLongText() {
            // Given
            String longText = "A".repeat(1000);

            // When
            String encrypted = encryptionService.encrypt(longText);
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertEquals(longText, decrypted);
        }

        @Test
        @DisplayName("Should handle special characters")
        void shouldHandleSpecialCharacters() {
            // Given
            String specialChars = "!@#$%^&*()_+-=[]{}|;:',.<>?/`~";

            // When
            String encrypted = encryptionService.encrypt(specialChars);
            String decrypted = encryptionService.decrypt(encrypted);

            // Then
            assertEquals(specialChars, decrypted);
        }
    }
}
