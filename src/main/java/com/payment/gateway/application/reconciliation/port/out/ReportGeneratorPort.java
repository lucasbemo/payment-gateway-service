package com.payment.gateway.application.reconciliation.port.out;

/**
 * Output port for report generation operations.
 */
public interface ReportGeneratorPort {

    String generateReport(String merchantId, String startDate, String endDate, String format);
}
