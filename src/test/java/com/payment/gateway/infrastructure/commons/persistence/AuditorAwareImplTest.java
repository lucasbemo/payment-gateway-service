package com.payment.gateway.infrastructure.commons.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuditorAwareImpl Tests")
class AuditorAwareImplTest {

    private AuditorAwareImpl auditorAware;

    @BeforeEach
    void setUp() {
        auditorAware = new AuditorAwareImpl();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("No Authentication")
    class NoAuthenticationTests {

        @Test
        @DisplayName("should return 'system' when no authentication is present")
        void shouldReturnSystemWhenNoAuthentication() {
            SecurityContextHolder.clearContext();

            Optional<String> auditor = auditorAware.getCurrentAuditor();

            assertThat(auditor).isPresent();
            assertThat(auditor.get()).isEqualTo("system");
        }

        @Test
        @DisplayName("should return 'system' when authentication is null")
        void shouldReturnSystemWhenAuthenticationIsNull() {
            SecurityContextHolder.getContext().setAuthentication(null);

            Optional<String> auditor = auditorAware.getCurrentAuditor();

            assertThat(auditor).isPresent();
            assertThat(auditor.get()).isEqualTo("system");
        }
    }

    @Nested
    @DisplayName("Anonymous User")
    class AnonymousUserTests {

        @Test
        @DisplayName("should return 'system' for anonymous user")
        void shouldReturnSystemForAnonymousUser() {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("anonymousUser", null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            Optional<String> auditor = auditorAware.getCurrentAuditor();

            assertThat(auditor).isPresent();
            assertThat(auditor.get()).isEqualTo("system");
        }
    }

    @Nested
    @DisplayName("Authenticated User")
    class AuthenticatedUserTests {

        @Test
        @DisplayName("should return principal name when authenticated")
        void shouldReturnPrincipalNameWhenAuthenticated() {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("john.doe", "password", Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            Optional<String> auditor = auditorAware.getCurrentAuditor();

            assertThat(auditor).isPresent();
            assertThat(auditor.get()).isEqualTo("john.doe");
        }

        @Test
        @DisplayName("should return correct name for different users")
        void shouldReturnCorrectNameForDifferentUsers() {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("admin@payment.com", "secret", Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            Optional<String> auditor = auditorAware.getCurrentAuditor();

            assertThat(auditor).isPresent();
            assertThat(auditor.get()).isEqualTo("admin@payment.com");
        }
    }

    @Nested
    @DisplayName("Unauthenticated Token")
    class UnauthenticatedTokenTests {

        @Test
        @DisplayName("should return 'system' when authentication is not authenticated")
        void shouldReturnSystemWhenNotAuthenticated() {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("user", "pass");
            // This token is not authenticated (no authorities constructor sets authenticated=false)
            SecurityContextHolder.getContext().setAuthentication(auth);

            Optional<String> auditor = auditorAware.getCurrentAuditor();

            assertThat(auditor).isPresent();
            assertThat(auditor.get()).isEqualTo("system");
        }
    }
}
