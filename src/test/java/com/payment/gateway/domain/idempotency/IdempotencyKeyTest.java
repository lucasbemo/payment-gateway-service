package com.payment.gateway.domain.idempotency;

import com.payment.gateway.domain.idempotency.model.IdempotencyKey;
import com.payment.gateway.domain.idempotency.model.IdempotencyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IdempotencyKey aggregate.
 */
class IdempotencyKeyTest {

    private static final String IDEMPOTENCY_KEY = "idem_key_123";
    private static final String MERCHANT_ID = "merch_456";
    private static final String OPERATION = "CREATE_PAYMENT";
    private static final String REQUEST_HASH = "hash_abc123";
    private static final int TTL_SECONDS = 3600;

    private IdempotencyKey key;

    @BeforeEach
    void setUp() {
        key = IdempotencyKey.create(IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, REQUEST_HASH, TTL_SECONDS);
    }

    @Nested
    @DisplayName("IdempotencyKey Creation")
    class Creation {

        @Test
        @DisplayName("Should create key with PENDING status")
        void shouldCreateKeyWithPendingStatus() {
            assertNotNull(key.getId());
            assertEquals(IDEMPOTENCY_KEY, key.getIdempotencyKey());
            assertEquals(MERCHANT_ID, key.getMerchantId());
            assertEquals(OPERATION, key.getOperation());
            assertEquals(IdempotencyStatus.PENDING, key.getStatus());
            assertNotNull(key.getExpiresAt());
        }

        @Test
        @DisplayName("Should create key with future expiresAt")
        void shouldCreateKeyWithFutureExpiresAt() {
            Instant expectedMin = Instant.now().plusSeconds(TTL_SECONDS - 1);
            assertTrue(key.getExpiresAt().isAfter(expectedMin));
        }
    }

    @Nested
    @DisplayName("Locking")
    class Locking {

        @Test
        @DisplayName("Should lock key with lock token")
        void shouldLockKeyWithLockToken() {
            String lockToken = "lock_token_xyz";

            key.lock(lockToken);

            assertEquals(IdempotencyStatus.PROCESSING, key.getStatus());
            assertEquals(lockToken, key.getLockToken());
            assertNotNull(key.getLockedAt());
        }

        @Test
        @DisplayName("Should return true for isLocked when lock token is set")
        void shouldBeLockedWhenLockTokenIsSet() {
            key.lock("token");

            assertTrue(key.isLocked());
        }

        @Test
        @DisplayName("Should release lock with correct token")
        void shouldReleaseLockWithCorrectToken() {
            String lockToken = "token_123";
            key.lock(lockToken);

            key.releaseLock(lockToken);

            assertEquals(IdempotencyStatus.PENDING, key.getStatus());
            assertNull(key.getLockToken());
            assertNull(key.getLockedAt());
        }

        @Test
        @DisplayName("Should not release lock with wrong token")
        void shouldNotReleaseLockWithWrongToken() {
            key.lock("token_123");

            key.releaseLock("wrong_token");

            assertTrue(key.isLocked());
            assertNotNull(key.getLockToken());
        }
    }

    @Nested
    @DisplayName("Completion")
    class Completion {

        @Test
        @DisplayName("Should complete key with response")
        void shouldCompleteKeyWithResponse() {
            key.lock("token");
            key.complete("200", "{\"paymentId\":\"pay_123\"}");

            assertEquals(IdempotencyStatus.COMPLETED, key.getStatus());
            assertEquals("200", key.getResponseCode());
            assertEquals("{\"paymentId\":\"pay_123\"}", key.getResponseBody());
            assertNotNull(key.getCompletedAt());
            assertNull(key.getLockToken());
        }

        @Test
        @DisplayName("Should fail key with error message")
        void shouldFailKeyWithErrorMessage() {
            key.lock("token");
            key.fail("Payment gateway timeout");

            assertEquals(IdempotencyStatus.FAILED, key.getStatus());
            assertEquals("Payment gateway timeout", key.getErrorMessage());
            assertNull(key.getLockToken());
        }
    }

    @Nested
    @DisplayName("Expiration")
    class Expiration {

        @Test
        @DisplayName("Should return false for isExpired when key is new")
        void shouldNotBeExpiredWhenKeyIsNew() {
            assertFalse(key.isExpired());
        }

        @Test
        @DisplayName("Should expire key when expire is called")
        void shouldExpireKeyWhenExpireIsCalled() {
            key.expire();

            assertEquals(IdempotencyStatus.EXPIRED, key.getStatus());
        }
    }

    @Nested
    @DisplayName("Terminal State")
    class TerminalState {

        @Test
        @DisplayName("Should return true for isTerminal when status is COMPLETED")
        void shouldBeTerminalWhenStatusIsCompleted() {
            key.lock("token");
            key.complete("200", "{}");

            assertTrue(key.isTerminal());
        }

        @Test
        @DisplayName("Should return true for isTerminal when status is FAILED")
        void shouldBeTerminalWhenStatusIsFailed() {
            key.fail("Error");

            assertTrue(key.isTerminal());
        }

        @Test
        @DisplayName("Should return true for isTerminal when status is EXPIRED")
        void shouldBeTerminalWhenStatusIsExpired() {
            key.expire();

            assertTrue(key.isTerminal());
        }

        @Test
        @DisplayName("Should return false for isTerminal when status is PENDING")
        void shouldNotBeTerminalWhenStatusIsPending() {
            assertFalse(key.isTerminal());
        }

        @Test
        @DisplayName("Should return false for isTerminal when status is PROCESSING")
        void shouldNotBeTerminalWhenStatusIsProcessing() {
            key.lock("token");

            assertFalse(key.isTerminal());
        }
    }

    @Nested
    @DisplayName("Builder")
    class Builder {

        @Test
        @DisplayName("Should build key with all fields")
        void shouldBuildKeyWithAllFields() {
            IdempotencyKey built = IdempotencyKey.builder()
                    .id("custom_id")
                    .idempotencyKey("custom_key")
                    .merchantId(MERCHANT_ID)
                    .operation(OPERATION)
                    .status(IdempotencyStatus.COMPLETED)
                    .responseCode("200")
                    .responseBody("{}")
                    .build();

            assertEquals("custom_id", built.getId());
            assertEquals("custom_key", built.getIdempotencyKey());
            assertEquals(IdempotencyStatus.COMPLETED, built.getStatus());
        }
    }
}
