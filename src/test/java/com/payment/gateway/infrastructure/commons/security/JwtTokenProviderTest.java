package com.payment.gateway.infrastructure.commons.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private static final String SECRET_KEY = "test-secret-key-for-jwt-signing-must-be-long-enough";
    private static final long EXPIRATION_MS = 3_600_000; // 1 hour

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET_KEY, EXPIRATION_MS);
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateTokenTests {

        @Test
        @DisplayName("should generate token with valid JWT format (3 parts separated by dots)")
        void shouldGenerateTokenWithValidFormat() {
            String token = tokenProvider.generateToken("user123", "ROLE_ADMIN");

            assertThat(token).isNotNull();
            String[] parts = token.split("\\.");
            assertThat(parts).hasSize(3);
        }

        @Test
        @DisplayName("should generate different tokens for different subjects")
        void shouldGenerateDifferentTokensForDifferentSubjects() {
            String token1 = tokenProvider.generateToken("user1", "ROLE_USER");
            String token2 = tokenProvider.generateToken("user2", "ROLE_USER");

            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("should generate different tokens for different roles")
        void shouldGenerateDifferentTokensForDifferentRoles() {
            String token1 = tokenProvider.generateToken("user1", "ROLE_USER");
            String token2 = tokenProvider.generateToken("user1", "ROLE_ADMIN");

            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("should generate non-empty token")
        void shouldGenerateNonEmptyToken() {
            String token = tokenProvider.generateToken("merchant-service", "ROLE_SERVICE");

            assertThat(token).isNotBlank();
            assertThat(token.length()).isGreaterThan(10);
        }
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateTokenTests {

        @Test
        @DisplayName("should return true for valid token")
        void shouldReturnTrueForValidToken() {
            String token = tokenProvider.generateToken("user123", "ROLE_USER");

            boolean valid = tokenProvider.validateToken(token);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("should return false for tampered token")
        void shouldReturnFalseForTamperedToken() {
            String token = tokenProvider.generateToken("user123", "ROLE_USER");
            // Tamper with the payload portion
            String tamperedToken = token.substring(0, token.lastIndexOf('.')) + ".invalidSignature";

            boolean valid = tokenProvider.validateToken(tamperedToken);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("should return false for token signed with different secret")
        void shouldReturnFalseForTokenWithDifferentSecret() {
            JwtTokenProvider otherProvider = new JwtTokenProvider("different-secret-key-for-testing", EXPIRATION_MS);
            String token = otherProvider.generateToken("user123", "ROLE_USER");

            boolean valid = tokenProvider.validateToken(token);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("should return false for expired token")
        void shouldReturnFalseForExpiredToken() {
            // Create a provider with negative expiration to generate an already-expired token
            JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET_KEY, -1000);
            String token = expiredProvider.generateToken("user123", "ROLE_USER");

            boolean valid = expiredProvider.validateToken(token);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("should return false for malformed token")
        void shouldReturnFalseForMalformedToken() {
            assertThat(tokenProvider.validateToken("not.a.valid.jwt.token")).isFalse();
            assertThat(tokenProvider.validateToken("completely-invalid")).isFalse();
            assertThat(tokenProvider.validateToken("")).isFalse();
        }

        @Test
        @DisplayName("should return false for token with wrong number of parts")
        void shouldReturnFalseForTokenWithWrongParts() {
            assertThat(tokenProvider.validateToken("only-one-part")).isFalse();
            assertThat(tokenProvider.validateToken("two.parts")).isFalse();
        }
    }

    @Nested
    @DisplayName("getSubject")
    class GetSubjectTests {

        @Test
        @DisplayName("should extract correct subject from token")
        void shouldExtractCorrectSubject() {
            String token = tokenProvider.generateToken("payment-user-42", "ROLE_USER");

            String subject = tokenProvider.getSubject(token);

            assertThat(subject).isEqualTo("payment-user-42");
        }

        @Test
        @DisplayName("should extract subject with special characters")
        void shouldExtractSubjectWithSpecialCharacters() {
            String token = tokenProvider.generateToken("user@example.com", "ROLE_ADMIN");

            String subject = tokenProvider.getSubject(token);

            assertThat(subject).isEqualTo("user@example.com");
        }
    }

    @Nested
    @DisplayName("getRole")
    class GetRoleTests {

        @Test
        @DisplayName("should extract correct role from token")
        void shouldExtractCorrectRole() {
            String token = tokenProvider.generateToken("user123", "ROLE_MERCHANT");

            String role = tokenProvider.getRole(token);

            assertThat(role).isEqualTo("ROLE_MERCHANT");
        }

        @Test
        @DisplayName("should extract different roles correctly")
        void shouldExtractDifferentRolesCorrectly() {
            String adminToken = tokenProvider.generateToken("admin", "ROLE_ADMIN");
            String userToken = tokenProvider.generateToken("user", "ROLE_USER");

            assertThat(tokenProvider.getRole(adminToken)).isEqualTo("ROLE_ADMIN");
            assertThat(tokenProvider.getRole(userToken)).isEqualTo("ROLE_USER");
        }
    }

    @Nested
    @DisplayName("End-to-end")
    class EndToEndTests {

        @Test
        @DisplayName("should generate, validate, and extract claims from token")
        void shouldSupportFullTokenLifecycle() {
            String subject = "merchant-service-001";
            String role = "ROLE_SERVICE";

            String token = tokenProvider.generateToken(subject, role);

            assertThat(tokenProvider.validateToken(token)).isTrue();
            assertThat(tokenProvider.getSubject(token)).isEqualTo(subject);
            assertThat(tokenProvider.getRole(token)).isEqualTo(role);
        }
    }
}
