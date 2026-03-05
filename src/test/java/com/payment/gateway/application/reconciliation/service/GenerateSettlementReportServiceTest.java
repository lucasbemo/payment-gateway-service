package com.payment.gateway.application.reconciliation.service;

import com.payment.gateway.application.reconciliation.dto.SettlementReportDTO;
import com.payment.gateway.application.reconciliation.port.out.ReportGeneratorPort;
import com.payment.gateway.application.reconciliation.port.out.SettlementReportPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.reconciliation.model.SettlementReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("Generate Settlement Report Service Tests")
@ExtendWith(MockitoExtension.class)
class GenerateSettlementReportServiceTest {

    @Mock
    private ReportGeneratorPort reportGeneratorPort;

    @Mock
    private SettlementReportPort settlementReportPort;

    private GenerateSettlementReportService generateSettlementReportService;

    @BeforeEach
    void setUp() {
        generateSettlementReportService = new GenerateSettlementReportService(reportGeneratorPort, settlementReportPort);
    }

    @Nested
    @DisplayName("Successful Report Generation")
    class SuccessfulGenerationTests {

        @Test
        @DisplayName("Should generate settlement report successfully")
        void shouldGenerateSettlementReportSuccessfully() {
            // Given
            String merchantId = "merchant-123";
            String startDate = "2024-01-01";
            String endDate = "2024-01-31";
            String format = "PDF";
            String filePath = "/reports/settlement-123.pdf";

            given(reportGeneratorPort.generateReport(merchantId, startDate, endDate, format)).willReturn(filePath);
            given(settlementReportPort.saveReport(any())).willAnswer(invocation -> invocation.getArgument(0));

            // When
            SettlementReportDTO response = generateSettlementReportService.generateSettlementReport(
                    merchantId, startDate, endDate, format);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMerchantId()).isEqualTo(merchantId);

            then(reportGeneratorPort).should().generateReport(merchantId, startDate, endDate, format);
            then(settlementReportPort).should().saveReport(any());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when start date is after end date")
        void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
            // Given
            String merchantId = "merchant-123";
            String startDate = "2024-01-31";
            String endDate = "2024-01-01";

            // When & Then
            assertThatThrownBy(() -> generateSettlementReportService.generateSettlementReport(
                    merchantId, startDate, endDate, "PDF"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Start date must be before end date");
        }

        @Test
        @DisplayName("Should throw exception when date format is invalid")
        void shouldThrowExceptionWhenDateFormatIsInvalid() {
            // Given
            String merchantId = "merchant-123";
            String startDate = "invalid-date";
            String endDate = "2024-01-31";

            // When & Then
            assertThatThrownBy(() -> generateSettlementReportService.generateSettlementReport(
                    merchantId, startDate, endDate, "PDF"))
                    .isInstanceOf(Exception.class);
        }
    }
}
