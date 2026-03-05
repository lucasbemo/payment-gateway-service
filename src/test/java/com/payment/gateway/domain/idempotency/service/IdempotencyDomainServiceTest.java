package com.payment.gateway.domain.idempotency.service;

import com.payment.gateway.domain.idempotency.model.IdempotencyKey;
import com.payment.gateway.domain.idempotency.model.IdempotencyStatus;
import com.payment.gateway.domain.idempotency.port.IdempotencyKeyRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdempotencyDomainService Tests")
class IdempotencyDomainServiceTest {

    @Mock
    private IdempotencyKeyRepositoryPort repository;

    private IdempotencyDomainService idempotencyDomainService;

    private final String IDEMPOTENCY_KEY = "idem_abc123";
    private final String MERCHANT_ID = "merch_123";
    private final String OPERATION = "PROCESS_PAYMENT";
    private final String REQUEST_BODY = "{\"amount\":1500.00}";
    private final String LOCK_TOKEN = "lock_token_xyz";
    private final int TTL_SECONDS = 86400;

    @BeforeEach
    void setUp() {
        idempotencyDomainService = new IdempotencyDomainService(repository, TTL_SECONDS);
    }

    @Nested
    @DisplayName("Create Or Get Idempotency Key")
    class CreateOrGetIdempotencyKeyTests {

        @Test
        @DisplayName("Should create new idempotency key when it does not exist")
        void shouldCreateNewIdempotencyKeyWhenItDoesNotExist() {
            // Given
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.empty());
            IdempotencyKey newKey = IdempotencyKey.create(IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, "hash123", TTL_SECONDS);
            given(repository.save(any(IdempotencyKey.class))).willReturn(newKey);

            // When
            IdempotencyKey result = idempotencyDomainService.createOrGetIdempotencyKey(
                IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, REQUEST_BODY
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(IdempotencyStatus.PENDING);
            verify(repository).findByIdempotencyKey(IDEMPOTENCY_KEY);
            verify(repository).save(any(IdempotencyKey.class));
        }

        @Test
        @DisplayName("Should return existing idempotency key when already exists")
        void shouldReturnExistingIdempotencyKeyWhenAlreadyExists() {
            // Given
            IdempotencyKey existingKey = IdempotencyKey.create(IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, "hash123", TTL_SECONDS);
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(existingKey));

            // When
            IdempotencyKey result = idempotencyDomainService.createOrGetIdempotencyKey(
                IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, REQUEST_BODY
            );

            // Then
            assertThat(result).isEqualTo(existingKey);
            verify(repository).findByIdempotencyKey(IDEMPOTENCY_KEY);
            verify(repository, never()).save(any(IdempotencyKey.class));
        }

        @Test
        @DisplayName("Should return existing key when it has completed response")
        void shouldReturnExistingKeyWhenHasCompletedResponse() {
            // Given
            IdempotencyKey existingKey = IdempotencyKey.create(IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, "hash123", TTL_SECONDS);
            existingKey.complete("200", "{\"paymentId\":\"pay_123\"}");
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(existingKey));

            // When
            IdempotencyKey result = idempotencyDomainService.createOrGetIdempotencyKey(
                IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, REQUEST_BODY
            );

            // Then
            assertThat(result).isEqualTo(existingKey);
            assertThat(result.getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
            verify(repository).findByIdempotencyKey(IDEMPOTENCY_KEY);
        }

        @Test
        @DisplayName("Should throw exception when idempotency key is expired")
        void shouldThrowExceptionWhenIdempotencyKeyIsExpired() {
            // Given
            IdempotencyKey expiredKey = IdempotencyKey.create(IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, "hash123", -1);
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(expiredKey));

            // When & Then
            assertThatThrownBy(() -> idempotencyDomainService.createOrGetIdempotencyKey(
                IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, REQUEST_BODY
            ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
        }
    }

    @Nested
    @DisplayName("Acquire Lock")
    class AcquireLockTests {

        @Test
        @DisplayName("Should acquire lock successfully when key is not locked")
        void shouldAcquireLockSuccessfullyWhenKeyIsNotLocked() {
            // Given
            IdempotencyKey key = IdempotencyKey.create(IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, "hash123", TTL_SECONDS);
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(key));
            given(repository.save(any(IdempotencyKey.class))).willReturn(key);

            // When
            IdempotencyKey result = idempotencyDomainService.acquireLock(IDEMPOTENCY_KEY, LOCK_TOKEN);

            // Then
            assertThat(result).isEqualTo(key);
            assertThat(result.isLocked()).isTrue();
            verify(repository).findByIdempotencyKey(IDEMPOTENCY_KEY);
            verify(repository).save(key);
        }

        @Test
        @DisplayName("Should throw exception when key is not found")
        void shouldThrowExceptionWhenKeyIsNotFound() {
            // Given
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> idempotencyDomainService.acquireLock(IDEMPOTENCY_KEY, LOCK_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Idempotency key not found");
        }

        @Test
        @DisplayName("Should throw exception when key is already locked with different token")
        void shouldThrowExceptionWhenKeyIsAlreadyLockedWithDifferentToken() {
            // Given
            IdempotencyKey key = IdempotencyKey.create(IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, "hash123", TTL_SECONDS);
            key.lock("other_lock_token");
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(key));

            // When & Then
            assertThatThrownBy(() -> idempotencyDomainService.acquireLock(IDEMPOTENCY_KEY, LOCK_TOKEN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already locked");
        }

        @Test
        @DisplayName("Should throw exception when key is terminal")
        void shouldThrowExceptionWhenKeyIsTerminal() {
            // Given
            IdempotencyKey key = IdempotencyKey.create(IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, "hash123", TTL_SECONDS);
            key.complete("200", "{\"paymentId\":\"pay_123\"}");
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(key));

            // When & Then
            assertThatThrownBy(() -> idempotencyDomainService.acquireLock(IDEMPOTENCY_KEY, LOCK_TOKEN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot lock terminal");
        }
    }

    @Nested
    @DisplayName("Complete With Response")
    class CompleteWithResponseTests {

        @Test
        @DisplayName("Should complete idempotency key with response successfully")
        void shouldCompleteIdempotencyKeyWithResponseSuccessfully() {
            // Given
            IdempotencyKey key = IdempotencyKey.create(IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, "hash123", TTL_SECONDS);
            key.lock(LOCK_TOKEN);
            String responseCode = "200";
            String responseBody = "{\"paymentId\":\"pay_123\"}";
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(key));
            given(repository.save(any(IdempotencyKey.class))).willReturn(key);

            // When
            IdempotencyKey result = idempotencyDomainService.completeWithResponse(
                IDEMPOTENCY_KEY, LOCK_TOKEN, responseCode, responseBody
            );

            // Then
            assertThat(result).isEqualTo(key);
            assertThat(result.getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
            verify(repository).findByIdempotencyKey(IDEMPOTENCY_KEY);
            verify(repository).save(key);
        }

        @Test
        @DisplayName("Should throw exception when key is not found")
        void shouldThrowExceptionWhenKeyIsNotFoundForComplete() {
            // Given
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> idempotencyDomainService.completeWithResponse(
                IDEMPOTENCY_KEY, LOCK_TOKEN, "200", "{\"response\":\"ok\"}"
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Idempotency key not found");
        }

        @Test
        @DisplayName("Should throw exception when lock token does not match")
        void shouldThrowExceptionWhenLockTokenDoesNotMatch() {
            // Given
            IdempotencyKey key = IdempotencyKey.create(IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, "hash123", TTL_SECONDS);
            key.lock("other_lock_token");
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(key));

            // When & Then
            assertThatThrownBy(() -> idempotencyDomainService.completeWithResponse(
                IDEMPOTENCY_KEY, LOCK_TOKEN, "200", "{\"response\":\"ok\"}"
            ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid lock token");
        }
    }

    @Nested
    @DisplayName("Fail With Error")
    class FailWithErrorTests {

        @Test
        @DisplayName("Should fail idempotency key with error message")
        void shouldFailIdempotencyKeyWithErrorMessage() {
            // Given
            IdempotencyKey key = IdempotencyKey.create(IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, "hash123", TTL_SECONDS);
            key.lock(LOCK_TOKEN);
            String errorMessage = "Payment processing failed";
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(key));
            given(repository.save(any(IdempotencyKey.class))).willReturn(key);

            // When
            IdempotencyKey result = idempotencyDomainService.failWithError(IDEMPOTENCY_KEY, LOCK_TOKEN, errorMessage);

            // Then
            assertThat(result).isEqualTo(key);
            assertThat(result.getStatus()).isEqualTo(IdempotencyStatus.FAILED);
            verify(repository).findByIdempotencyKey(IDEMPOTENCY_KEY);
            verify(repository).save(key);
        }
    }

    @Nested
    @DisplayName("Release Lock")
    class ReleaseLockTests {

        @Test
        @DisplayName("Should release lock successfully")
        void shouldReleaseLockSuccessfully() {
            // Given
            IdempotencyKey key = IdempotencyKey.create(IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, "hash123", TTL_SECONDS);
            key.lock(LOCK_TOKEN);
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(key));
            given(repository.save(any(IdempotencyKey.class))).willReturn(key);

            // When
            IdempotencyKey result = idempotencyDomainService.releaseLock(IDEMPOTENCY_KEY, LOCK_TOKEN);

            // Then
            assertThat(result).isEqualTo(key);
            verify(repository).findByIdempotencyKey(IDEMPOTENCY_KEY);
            verify(repository).save(key);
        }

        @Test
        @DisplayName("Should throw exception when key is not found")
        void shouldThrowExceptionWhenKeyIsNotFoundForRelease() {
            // Given
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> idempotencyDomainService.releaseLock(IDEMPOTENCY_KEY, LOCK_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Idempotency key not found");
        }
    }

    @Nested
    @DisplayName("Get Idempotency Key")
    class GetIdempotencyKeyTests {

        @Test
        @DisplayName("Should return idempotency key when found")
        void shouldReturnIdempotencyKeyWhenFound() {
            // Given
            IdempotencyKey key = IdempotencyKey.create(IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, "hash123", TTL_SECONDS);
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(key));

            // When
            Optional<IdempotencyKey> result = idempotencyDomainService.getIdempotencyKey(IDEMPOTENCY_KEY);

            // Then
            assertThat(result).isPresent().contains(key);
        }

        @Test
        @DisplayName("Should return empty when key is not found")
        void shouldReturnEmptyWhenKeyIsNotFound() {
            // Given
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.empty());

            // When
            Optional<IdempotencyKey> result = idempotencyDomainService.getIdempotencyKey(IDEMPOTENCY_KEY);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return key with orThrow method when found")
        void shouldReturnKeyWithOrThrowMethodWhenFound() {
            // Given
            IdempotencyKey key = IdempotencyKey.create(IDEMPOTENCY_KEY, MERCHANT_ID, OPERATION, "hash123", TTL_SECONDS);
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(key));

            // When
            IdempotencyKey result = idempotencyDomainService.getIdempotencyKeyOrThrow(IDEMPOTENCY_KEY);

            // Then
            assertThat(result).isEqualTo(key);
        }

        @Test
        @DisplayName("Should throw exception when key is not found with orThrow method")
        void shouldThrowExceptionWhenKeyIsNotFoundWithOrThrowMethod() {
            // Given
            given(repository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> idempotencyDomainService.getIdempotencyKeyOrThrow(IDEMPOTENCY_KEY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Idempotency key not found");
        }
    }

    @Nested
    @DisplayName("Exists")
    class ExistsTests {

        @Test
        @DisplayName("Should return true when idempotency key exists")
        void shouldReturnTrueWhenIdempotencyKeyExists() {
            // Given
            given(repository.existsByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(true);

            // When
            boolean result = idempotencyDomainService.exists(IDEMPOTENCY_KEY);

            // Then
            assertThat(result).isTrue();
            verify(repository).existsByIdempotencyKey(IDEMPOTENCY_KEY);
        }

        @Test
        @DisplayName("Should return false when idempotency key does not exist")
        void shouldReturnFalseWhenIdempotencyKeyDoesNotExist() {
            // Given
            given(repository.existsByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(false);

            // When
            boolean result = idempotencyDomainService.exists(IDEMPOTENCY_KEY);

            // Then
            assertThat(result).isFalse();
            verify(repository).existsByIdempotencyKey(IDEMPOTENCY_KEY);
        }
    }
}
