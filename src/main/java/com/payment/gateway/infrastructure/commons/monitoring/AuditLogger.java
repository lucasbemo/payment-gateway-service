package com.payment.gateway.infrastructure.commons.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Audit logger for security and compliance.
 * Logs all payment operations, data access, and security events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogger {

    private static final String AUDIT_LOG_NAME = "AUDIT";

    /**
     * Log a payment operation.
     */
    public void logPaymentOperation(String paymentId, String merchantId, String operation, String status, long amount) {
        log.info(AUDIT_LOG_NAME, "PAYMENT_OPERATION | paymentId={} | merchantId={} | operation={} | status={} | amount={}",
                paymentId, merchantId, operation, status, amount);
    }

    /**
     * Log a refund operation.
     */
    public void logRefundOperation(String refundId, String paymentId, String merchantId, String status, long amount) {
        log.info(AUDIT_LOG_NAME, "REFUND_OPERATION | refundId={} | paymentId={} | merchantId={} | status={} | amount={}",
                refundId, paymentId, merchantId, status, amount);
    }

    /**
     * Log a transaction event.
     */
    public void logTransactionEvent(String transactionId, String eventType, String status) {
        log.info(AUDIT_LOG_NAME, "TRANSACTION_EVENT | transactionId={} | eventType={} | status={}",
                transactionId, eventType, status);
    }

    /**
     * Log a security event.
     */
    public void logSecurityEvent(String eventType, String principal, String resource, String outcome) {
        log.info(AUDIT_LOG_NAME, "SECURITY_EVENT | eventType={} | principal={} | resource={} | outcome={}",
                eventType, principal, resource, outcome);
    }

    /**
     * Log a data access event.
     */
    public void logDataAccess(String entityType, String entityId, String operation, String userId) {
        log.info(AUDIT_LOG_NAME, "DATA_ACCESS | entityType={} | entityId={} | operation={} | userId={}",
                entityType, entityId, operation, userId);
    }

    /**
     * Log an authentication event.
     */
    public void logAuthenticationEvent(String principal, String outcome, String reason) {
        log.info(AUDIT_LOG_NAME, "AUTHENTICATION | principal={} | outcome={} | reason={}",
                principal, outcome, reason);
    }

    /**
     * Log an API access event.
     */
    public void logApiAccess(String method, String path, String userId, String status) {
        log.info(AUDIT_LOG_NAME, "API_ACCESS | method={} | path={} | userId={} | status={}",
                method, path, userId, status);
    }

    /**
     * Log a rate limiting event.
     */
    public void logRateLimitEvent(String merchantId, int attempts, String action) {
        log.info(AUDIT_LOG_NAME, "RATE_LIMIT | merchantId={} | attempts={} | action={}",
                merchantId, attempts, action);
    }

    /**
     * Log a circuit breaker event.
     */
    public void logCircuitBreakerEvent(String serviceName, String state, String reason) {
        log.info(AUDIT_LOG_NAME, "CIRCUIT_BREAKER | serviceName={} | state={} | reason={}",
                serviceName, state, reason);
    }
}
