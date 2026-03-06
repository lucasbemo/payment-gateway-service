package com.payment.gateway.infrastructure.commons.persistence;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration for pessimistic locking strategies.
 * Provides constants and utilities for JPA pessimistic lock management.
 */
@Configuration
public class PessimisticLockingConfig {

    public static final long DEFAULT_LOCK_TIMEOUT_MS = 5000;

    public static final String LOCK_TIMEOUT_HINT = "jakarta.persistence.lock.timeout";
}
