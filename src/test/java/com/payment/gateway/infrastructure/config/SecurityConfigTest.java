package com.payment.gateway.infrastructure.config;

import com.payment.gateway.infrastructure.commons.security.RbacConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SecurityConfig and RbacConfig.
 */
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Nested
    @DisplayName("Bean Configuration Tests")
    class BeanConfigurationTests {

        private final SecurityConfig securityConfig = new SecurityConfig();

        @Test
        @DisplayName("Should create PasswordEncoder bean")
        void shouldCreatePasswordEncoderBean() {
            // When
            PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

            // Then
            assertNotNull(passwordEncoder);
            assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
        }

        @Test
        @DisplayName("Should encode password with BCrypt")
        void shouldEncodePasswordWithBCrypt() {
            // Given
            PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
            String rawPassword = "test-secret-password";

            // When
            String encoded = passwordEncoder.encode(rawPassword);

            // Then
            assertNotNull(encoded);
            assertNotEquals(rawPassword, encoded);
            assertTrue(encoded.startsWith("$2a$") || encoded.startsWith("$2b$"));
        }

        @Test
        @DisplayName("Should match raw password with encoded password")
        void shouldMatchRawPasswordWithEncodedPassword() {
            // Given
            PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
            String rawPassword = "test-secret-password";
            String encoded = passwordEncoder.encode(rawPassword);

            // When
            boolean matches = passwordEncoder.matches(rawPassword, encoded);

            // Then
            assertTrue(matches);
        }

        @Test
        @DisplayName("Should not match wrong password")
        void shouldNotMatchWrongPassword() {
            // Given
            PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
            String rawPassword = "test-secret-password";
            String wrongPassword = "wrong-password";
            String encoded = passwordEncoder.encode(rawPassword);

            // When
            boolean matches = passwordEncoder.matches(wrongPassword, encoded);

            // Then
            assertFalse(matches);
        }

        @Test
        @DisplayName("Should create CorsConfigurationSource bean")
        void shouldCreateCorsConfigurationSourceBean() {
            // When
            var corsSource = securityConfig.corsConfigurationSource();

            // Then
            assertNotNull(corsSource);
        }
    }

    @Nested
    @DisplayName("RbacConfig Tests")
    class RbacConfigTests {

        @Test
        @DisplayName("Should have ADMIN role defined")
        void shouldHaveAdminRoleDefined() {
            assertEquals("ADMIN", RbacConfig.ROLE_ADMIN);
        }

        @Test
        @DisplayName("Should have MERCHANT role defined")
        void shouldHaveMerchantRoleDefined() {
            assertEquals("MERCHANT", RbacConfig.ROLE_MERCHANT);
        }

        @Test
        @DisplayName("Should have DEVELOPER role defined")
        void shouldHaveDeveloperRoleDefined() {
            assertEquals("DEVELOPER", RbacConfig.ROLE_DEVELOPER);
        }

        @Test
        @DisplayName("Should have distinct roles")
        void shouldHaveDistinctRoles() {
            assertNotEquals(RbacConfig.ROLE_ADMIN, RbacConfig.ROLE_MERCHANT);
            assertNotEquals(RbacConfig.ROLE_ADMIN, RbacConfig.ROLE_DEVELOPER);
            assertNotEquals(RbacConfig.ROLE_MERCHANT, RbacConfig.ROLE_DEVELOPER);
        }
    }
}
