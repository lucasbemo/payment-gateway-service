package com.payment.gateway.application.reconciliation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.payment.gateway.application.reconciliation.dto.SettlementReportDTO;
import com.payment.gateway.application.reconciliation.port.out.ReportGeneratorPort;
import com.payment.gateway.application.reconciliation.port.out.SettlementReportPort;
import com.payment.gateway.application.transaction.port.out.TransactionQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.transaction.model.Transaction;
import com.payment.gateway.domain.transaction.model.TransactionStatus;
import com.payment.gateway.domain.transaction.model.TransactionType;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("Generate Settlement Report Service Tests")
@ExtendWith(MockitoExtension.class)
class GenerateSettlementReportServiceTest {

    @Mock
    private ReportGeneratorPort reportGeneratorPort;

    @Mock
    private SettlementReportPort settlementReportPort;

    @Mock
    private TransactionQueryPort transactionQueryPort;

    private GenerateSettlementReportService generateSettlementReportService;

    @BeforeEach
    void setUp() {
        generateSettlementReportService =
                new GenerateSettlementReportService(reportGeneratorPort, settlementReportPort, transactionQueryPort);
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

            given(transactionQueryPort.findByMerchantIdAndCreatedAtBetween(eq(merchantId), any(), any()))
                    .willReturn(List.of(createTransaction(merchantId, 1000L), createTransaction(merchantId, 2500L)));
            given(reportGeneratorPort.generateReport(merchantId, startDate, endDate, format))
                    .willReturn(filePath);
            given(settlementReportPort.saveReport(any())).willAnswer(invocation -> invocation.getArgument(0));

            // When
            SettlementReportDTO response =
                    generateSettlementReportService.generateSettlementReport(merchantId, startDate, endDate, format);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMerchantId()).isEqualTo(merchantId);
            assertThat(response.getTotalAmount()).isEqualTo(3500L);
            assertThat(response.getTransactionCount()).isEqualTo(2);

            then(reportGeneratorPort).should().generateReport(merchantId, startDate, endDate, format);
            then(settlementReportPort).should().saveReport(any());
        }

        private Transaction createTransaction(String merchantId, long amountInCents) {
            Instant now = Instant.now();
            return Transaction.builder()
                    .id(java.util.UUID.randomUUID().toString())
                    .paymentId("payment-123")
                    .merchantId(merchantId)
                    .type(TransactionType.PAYMENT)
                    .amount(Money.of(amountInCents, Currency.getInstance("USD")))
                    .currency("USD")
                    .status(TransactionStatus.CAPTURED)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
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
