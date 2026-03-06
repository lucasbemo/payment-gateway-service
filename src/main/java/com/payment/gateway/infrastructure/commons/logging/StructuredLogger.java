package com.payment.gateway.infrastructure.commons.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Structured logger utility that enriches log messages with contextual metadata.
 */
@Slf4j
@Component
public class StructuredLogger {

    public void logPaymentEvent(String eventType, String paymentId, String merchantId, Map<String, String> extra) {
        try {
            MDC.put("eventType", eventType);
            MDC.put("paymentId", paymentId);
            MDC.put("merchantId", merchantId);
            if (extra != null) {
                extra.forEach(MDC::put);
            }
            log.info("Payment event: {} paymentId={} merchantId={}", eventType, paymentId, merchantId);
        } finally {
            MDC.remove("eventType");
            MDC.remove("paymentId");
            MDC.remove("merchantId");
            if (extra != null) {
                extra.keySet().forEach(MDC::remove);
            }
        }
    }

    public void logTransactionEvent(String eventType, String transactionId, Map<String, String> extra) {
        try {
            MDC.put("eventType", eventType);
            MDC.put("transactionId", transactionId);
            if (extra != null) {
                extra.forEach(MDC::put);
            }
            log.info("Transaction event: {} transactionId={}", eventType, transactionId);
        } finally {
            MDC.remove("eventType");
            MDC.remove("transactionId");
            if (extra != null) {
                extra.keySet().forEach(MDC::remove);
            }
        }
    }

    public void logSecurityEvent(String eventType, String subject, String action) {
        try {
            MDC.put("eventType", eventType);
            MDC.put("subject", subject);
            MDC.put("action", action);
            log.info("Security event: {} subject={} action={}", eventType, subject, action);
        } finally {
            MDC.remove("eventType");
            MDC.remove("subject");
            MDC.remove("action");
        }
    }
}
