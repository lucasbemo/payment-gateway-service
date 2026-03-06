package com.payment.gateway.infrastructure.reconciliation.adapter.out.provider;

import com.payment.gateway.application.reconciliation.port.out.ReportGeneratorPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StubReportGenerator implements ReportGeneratorPort {

    @Override
    public String generateReport(String merchantId, String startDate, String endDate, String format) {
        log.info("StubReportGenerator.generateReport: merchantId={}, startDate={}, endDate={}, format={}",
                merchantId, startDate, endDate, format);
        return "/reports/" + merchantId + "_" + startDate + "_" + endDate + "." + format.toLowerCase();
    }
}
