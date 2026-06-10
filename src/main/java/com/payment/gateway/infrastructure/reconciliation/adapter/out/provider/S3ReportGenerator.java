package com.payment.gateway.infrastructure.reconciliation.adapter.out.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.application.reconciliation.port.out.ReportGeneratorPort;
import com.payment.gateway.application.transaction.port.out.TransactionQueryPort;
import com.payment.gateway.domain.transaction.model.Transaction;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Settlement report generator that uploads real JSON reports to S3-compatible storage (MinIO).
 * Active outside the test/e2e profiles, where the stub generator is used instead.
 */
@Slf4j
@Component
@Primary
@Profile("!test & !e2e")
@RequiredArgsConstructor
public class S3ReportGenerator implements ReportGeneratorPort {

    static final String BUCKET_NAME = "settlement-reports";

    private final S3Client s3Client;
    private final TransactionQueryPort transactionQueryPort;
    private final ObjectMapper objectMapper;

    @Override
    public String generateReport(String merchantId, String startDate, String endDate, String format) {
        log.info(
                "Generating S3 settlement report: merchantId={}, startDate={}, endDate={}, format={}",
                merchantId,
                startDate,
                endDate,
                format);

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<Transaction> transactions = transactionQueryPort.findByMerchantIdAndCreatedAtBetween(
                merchantId,
                start.atStartOfDay(ZoneOffset.UTC).toInstant(),
                end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());

        String content = buildReportContent(merchantId, startDate, endDate, transactions);
        String key = merchantId + "/" + startDate + "_" + endDate + ".json";

        ensureBucketExists();
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(key)
                        .contentType("application/json")
                        .build(),
                RequestBody.fromString(content));

        String location = "s3://" + BUCKET_NAME + "/" + key;
        log.info("Settlement report uploaded to {} ({} transactions)", location, transactions.size());
        return location;
    }

    private String buildReportContent(
            String merchantId, String startDate, String endDate, List<Transaction> transactions) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("merchantId", merchantId);

        Map<String, Object> period = new LinkedHashMap<>();
        period.put("startDate", startDate);
        period.put("endDate", endDate);
        report.put("period", period);

        report.put(
                "transactions", transactions.stream().map(this::toReportEntry).toList());

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize settlement report for merchant " + merchantId, e);
        }
    }

    private Map<String, Object> toReportEntry(Transaction transaction) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("id", transaction.getId());
        entry.put("paymentId", transaction.getPaymentId());
        entry.put("type", transaction.getType() != null ? transaction.getType().name() : null);
        entry.put(
                "status",
                transaction.getStatus() != null ? transaction.getStatus().name() : null);
        entry.put(
                "amount",
                transaction.getAmount() != null ? transaction.getAmount().getAmountInCents() : null);
        entry.put("currency", transaction.getCurrency());
        entry.put(
                "createdAt",
                transaction.getCreatedAt() != null ? transaction.getCreatedAt().toString() : null);
        return entry;
    }

    private void ensureBucketExists() {
        try {
            s3Client.createBucket(
                    CreateBucketRequest.builder().bucket(BUCKET_NAME).build());
            log.info("Created S3 bucket {}", BUCKET_NAME);
        } catch (BucketAlreadyOwnedByYouException | BucketAlreadyExistsException e) {
            log.debug("S3 bucket {} already exists", BUCKET_NAME);
        }
    }
}
