package com.payment.gateway.application.commons.port.out;

public interface AuditPort {
    
    void logPaymentOperation(String paymentId, String merchantId, String operation, String status, long amount);
    
    void logRefundOperation(String refundId, String paymentId, String merchantId, String status, long amount);
    
    void logSecurityEvent(String eventType, String principal, String resource, String outcome);
    
    void logDataAccess(String entityType, String entityId, String operation, String userId);
}