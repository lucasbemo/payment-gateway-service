package com.payment.gateway.infrastructure.commons.persistence;

import org.springframework.context.annotation.Configuration;

/**
 * HikariCP connection pool configuration.
 * Pool settings are defined in application.yml under spring.datasource.hikari.
 *
 * Default recommended settings:
 * - maximum-pool-size: 20
 * - minimum-idle: 5
 * - idle-timeout: 300000 (5 min)
 * - connection-timeout: 20000 (20 sec)
 * - max-lifetime: 1800000 (30 min)
 * - leak-detection-threshold: 60000 (1 min)
 */
@Configuration
public class HikariPoolConfig {

    public static final int DEFAULT_MAX_POOL_SIZE = 20;
    public static final int DEFAULT_MIN_IDLE = 5;
    public static final long DEFAULT_IDLE_TIMEOUT_MS = 300_000;
    public static final long DEFAULT_CONNECTION_TIMEOUT_MS = 20_000;
    public static final long DEFAULT_MAX_LIFETIME_MS = 1_800_000;
    public static final long DEFAULT_LEAK_DETECTION_THRESHOLD_MS = 60_000;
}
