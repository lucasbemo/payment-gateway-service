package com.payment.gateway.commons.constants;

/**
 * Application-wide constants.
 */
public final class AppConstants {

    private AppConstants() {
        // Private constructor to prevent instantiation
    }

    // API Paths
    public static final String API_V1 = "/api/v1";
    public static final String API_HEALTH = "/actuator/health";
    public static final String API_METRICS = "/actuator/metrics";
    public static final String API_SWAGGER = "/swagger-ui.html";
    public static final String API_OPENAPI = "/v3/api-docs";

    // HTTP Headers
    public static final String HEADER_API_KEY = "X-API-Key";
    public static final String HEADER_IDEMPOTENCY_KEY = "X-Idempotency-Key";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    // Payment Constants
    public static final int MAX_PAYMENT_DESCRIPTION_LENGTH = 1024;
    public static final int MAX_REFUND_REASON_LENGTH = 1024;
    public static final int MAX_IDEMPOTENCY_KEY_LENGTH = 255;

    // Database Constants
    public static final int UUID_LENGTH = 36;
    public static final int HASH_LENGTH = 64; // SHA-256 hex length

    // Kafka Topics
    public static final String TOPIC_PAYMENT_CREATED = "payment.created";
    public static final String TOPIC_PAYMENT_COMPLETED = "payment.completed";
    public static final String TOPIC_PAYMENT_FAILED = "payment.failed";
    public static final String TOPIC_PAYMENT_CANCELLED = "payment.cancelled";
    public static final String TOPIC_REFUND_PROCESSED = "refund.processed";
    public static final String TOPIC_REFUND_FAILED = "refund.failed";
    public static final String TOPIC_OUTBOX_EVENTS = "outbox.events";

    // Redis Keys
    public static final String REDIS_IDEMPOTENCY_PREFIX = "idempotency:";
    public static final String REDIS_LOCK_PREFIX = "lock:";
    public static final long REDIS_DEFAULT_TTL_SECONDS = 3600; // 1 hour

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String SORT_DIRECTION_ASC = "asc";
    public static final String SORT_DIRECTION_DESC = "desc";

    // Date/Time Patterns
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String ISO_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    // Error Codes
    public static final String ERROR_CODE_VALIDATION = "VALIDATION_ERROR";
    public static final String ERROR_CODE_NOT_FOUND = "NOT_FOUND";
    public static final String ERROR_CODE_BUSINESS = "BUSINESS_ERROR";
    public static final String ERROR_CODE_DUPLICATE = "DUPLICATE_ERROR";
    public static final String ERROR_CODE_INTERNAL = "INTERNAL_ERROR";
    public static final String ERROR_CODE_UNAUTHORIZED = "UNAUTHORIZED";
    public static final String ERROR_CODE_FORBIDDEN = "FORBIDDEN";

    // Status Values
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // Provider Names
    public static final String PROVIDER_STRIPE = "STRIPE";
    public static final String PROVIDER_PAYPAL = "PAYPAL";
    public static final String PROVIDER_MERCADO_PAGO = "MERCADO_PAGO";

    // Currency Codes
    public static final String CURRENCY_USD = "USD";
    public static final String CURRENCY_EUR = "EUR";
    public static final String CURRENCY_BRL = "BRL";

    // Timeouts (milliseconds)
    public static final long DEFAULT_TIMEOUT_MS = 30000;
    public static final long DB_TIMEOUT_MS = 5000;
    public static final long HTTP_TIMEOUT_MS = 10000;
    public static final long KAFKA_TIMEOUT_MS = 5000;
}
