package com.payment.gateway.infrastructure.commons.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Audit logger for security and compliance.
 * Logs all payment operations, data access, and security events.
 * Uses a dedicated "AUDIT" logger that writes to audit.log file.
 */
@Component
public class AuditLogger {

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");

    public void logPaymentOperation(String paymentId, String merchantId, String operation, String status, long amount) {
        AUDIT_LOG.info("PAYMENT_OPERATION | paymentId={} | merchantId={} | operation={} | status={} | amount={}",
                paymentId, merchantId, operation, status, amount);
    }

    public void logRefundOperation(String refundId, String paymentId, String merchantId, String status, long amount) {
        AUDIT_LOG.info("REFUND_OPERATION | refundId={} | paymentId={} | merchantId={} | status={} | amount={}",
                refundId, paymentId, merchantId, status, amount);
    }

    public void logTransactionEvent(String transactionId, String eventType, String status) {
        AUDIT_LOG.info("TRANSACTION_EVENT | transactionId={} | eventType={} | status={}",
                transactionId, eventType, status);
    }

    public void logSecurityEvent(String eventType, String principal, String resource, String outcome) {
        AUDIT_LOG.info("SECURITY_EVENT | eventType={} | principal={} | resource={} | outcome={}",
                eventType, principal, resource, outcome);
    }

    public void logDataAccess(String entityType, String entityId, String operation, String userId) {
        AUDIT_LOG.info("DATA_ACCESS | entityType={} | entityId={} | operation={} | userId={}",
                entityType, entityId, operation, userId);
    }

    public void logAuthenticationEvent(String principal, String outcome, String reason) {
        AUDIT_LOG.info("AUTHENTICATION | principal={} | outcome={} | reason={}",
                principal, outcome, reason);
    }

    public void logApiAccess(String method, String path, String userId, String status) {
        AUDIT_LOG.info("API_ACCESS | method={} | path={} | userId={} | status={}",
                method, path, userId, status);
    }

    public void logRateLimitEvent(String merchantId, int attempts, String action) {
        AUDIT_LOG.info("RATE_LIMIT | merchantId={} | attempts={} | action={}",
                merchantId, attempts, action);
    }

    public void logCircuitBreakerEvent(String serviceName, String state, String reason) {
        AUDIT_LOG.info("CIRCUIT_BREAKER | serviceName={} | state={} | reason={}",
                serviceName, state, reason);
    }
}