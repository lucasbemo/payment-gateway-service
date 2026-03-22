package com.payment.gateway.infrastructure.commons.monitoring;

import com.payment.gateway.application.commons.port.out.AuditPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditAdapter implements AuditPort {

    private final AuditLogger auditLogger;

    @Override
    public void logPaymentOperation(String paymentId, String merchantId, String operation, String status, long amount) {
        auditLogger.logPaymentOperation(paymentId, merchantId, operation, status, amount);
    }

    @Override
    public void logRefundOperation(String refundId, String paymentId, String merchantId, String status, long amount) {
        auditLogger.logRefundOperation(refundId, paymentId, merchantId, status, amount);
    }

    @Override
    public void logSecurityEvent(String eventType, String principal, String resource, String outcome) {
        auditLogger.logSecurityEvent(eventType, principal, resource, outcome);
    }

    @Override
    public void logDataAccess(String entityType, String entityId, String operation, String userId) {
        auditLogger.logDataAccess(entityType, entityId, operation, userId);
    }
}