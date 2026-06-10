package com.payment.gateway.infrastructure.webhook.adapter.out.delivery;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.payment.gateway.application.commons.port.out.AuditPort;
import com.payment.gateway.application.payment.port.out.MerchantQueryPort;
import com.payment.gateway.domain.merchant.model.Merchant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookDeliveryService Tests")
class WebhookDeliveryServiceTest {

    private static final String MERCHANT_ID = "merchant_123";
    private static final String WEBHOOK_URL = "https://merchant.example.com/webhooks";
    private static final String EVENT_TYPE = "PAYMENT_COMPLETED";
    private static final String PAYLOAD = "{\"eventType\":\"PAYMENT_COMPLETED\",\"aggregateId\":\"pay_123\"}";

    @Mock
    private MerchantQueryPort merchantQueryPort;

    @Mock
    private AuditPort auditPort;

    @Mock
    private RestTemplate restTemplate;

    private WebhookDeliveryService service;

    @BeforeEach
    void setUp() {
        // 1ms backoff so retry tests stay fast
        service = new WebhookDeliveryService(merchantQueryPort, auditPort, restTemplate, 3, 1L);
    }

    private Merchant merchantWithWebhook(String webhookUrl) {
        return Merchant.register(
                "Test Merchant",
                "merchant@example.com",
                "pk_test_123",
                "apiKeyHash",
                "apiSecretHash",
                webhookUrl,
                null);
    }

    @Test
    @DisplayName("Should deliver webhook successfully and audit SUCCESS")
    void shouldDeliverSuccessfully() {
        when(merchantQueryPort.findById(MERCHANT_ID)).thenReturn(Optional.of(merchantWithWebhook(WEBHOOK_URL)));
        when(restTemplate.postForEntity(eq(WEBHOOK_URL), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        service.deliver(MERCHANT_ID, EVENT_TYPE, PAYLOAD);

        verify(restTemplate, times(1)).postForEntity(eq(WEBHOOK_URL), any(HttpEntity.class), eq(String.class));
        verify(auditPort).logWebhookDelivery(MERCHANT_ID, EVENT_TYPE, WEBHOOK_URL, "SUCCESS", 1);
    }

    @Test
    @DisplayName("Should send required headers and payload body")
    void shouldSendHeadersAndPayload() {
        when(merchantQueryPort.findById(MERCHANT_ID)).thenReturn(Optional.of(merchantWithWebhook(WEBHOOK_URL)));
        var captor = org.mockito.ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.postForEntity(eq(WEBHOOK_URL), captor.capture(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        service.deliver(MERCHANT_ID, EVENT_TYPE, PAYLOAD);

        HttpEntity<?> sent = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(sent.getBody()).isEqualTo(PAYLOAD);
        org.assertj.core.api.Assertions.assertThat(sent.getHeaders().getContentType())
                .hasToString("application/json");
        org.assertj.core.api.Assertions.assertThat(sent.getHeaders().getFirst("X-Webhook-Event"))
                .isEqualTo(EVENT_TYPE);
        org.assertj.core.api.Assertions.assertThat(sent.getHeaders().getFirst("X-Webhook-Id"))
                .isNotBlank();
    }

    @Test
    @DisplayName("Should retry on IO error then succeed")
    void shouldRetryThenSucceed() {
        when(merchantQueryPort.findById(MERCHANT_ID)).thenReturn(Optional.of(merchantWithWebhook(WEBHOOK_URL)));
        when(restTemplate.postForEntity(eq(WEBHOOK_URL), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new ResourceAccessException("connection refused"))
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        service.deliver(MERCHANT_ID, EVENT_TYPE, PAYLOAD);

        verify(restTemplate, times(2)).postForEntity(eq(WEBHOOK_URL), any(HttpEntity.class), eq(String.class));
        verify(auditPort).logWebhookDelivery(MERCHANT_ID, EVENT_TYPE, WEBHOOK_URL, "SUCCESS", 2);
    }

    @Test
    @DisplayName("Should retry on 5xx then succeed")
    void shouldRetryOn5xxThenSucceed() {
        when(merchantQueryPort.findById(MERCHANT_ID)).thenReturn(Optional.of(merchantWithWebhook(WEBHOOK_URL)));
        when(restTemplate.postForEntity(eq(WEBHOOK_URL), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        service.deliver(MERCHANT_ID, EVENT_TYPE, PAYLOAD);

        verify(restTemplate, times(2)).postForEntity(eq(WEBHOOK_URL), any(HttpEntity.class), eq(String.class));
        verify(auditPort).logWebhookDelivery(MERCHANT_ID, EVENT_TYPE, WEBHOOK_URL, "SUCCESS", 2);
    }

    @Test
    @DisplayName("Should give up after 3 attempts and audit FAILED without throwing")
    void shouldGiveUpAfterThreeAttempts() {
        when(merchantQueryPort.findById(MERCHANT_ID)).thenReturn(Optional.of(merchantWithWebhook(WEBHOOK_URL)));
        when(restTemplate.postForEntity(eq(WEBHOOK_URL), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new ResourceAccessException("connection timed out"));

        assertThatCode(() -> service.deliver(MERCHANT_ID, EVENT_TYPE, PAYLOAD)).doesNotThrowAnyException();

        verify(restTemplate, times(3)).postForEntity(eq(WEBHOOK_URL), any(HttpEntity.class), eq(String.class));
        verify(auditPort).logWebhookDelivery(MERCHANT_ID, EVENT_TYPE, WEBHOOK_URL, "FAILED", 3);
    }

    @Test
    @DisplayName("Should not retry on 4xx and audit FAILED")
    void shouldNotRetryOn4xx() {
        when(merchantQueryPort.findById(MERCHANT_ID)).thenReturn(Optional.of(merchantWithWebhook(WEBHOOK_URL)));
        when(restTemplate.postForEntity(eq(WEBHOOK_URL), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThatCode(() -> service.deliver(MERCHANT_ID, EVENT_TYPE, PAYLOAD)).doesNotThrowAnyException();

        verify(restTemplate, times(1)).postForEntity(eq(WEBHOOK_URL), any(HttpEntity.class), eq(String.class));
        verify(auditPort).logWebhookDelivery(MERCHANT_ID, EVENT_TYPE, WEBHOOK_URL, "FAILED", 1);
    }

    @Test
    @DisplayName("Should skip silently when merchant has no webhookUrl")
    void shouldSkipWhenNoWebhookUrl() {
        when(merchantQueryPort.findById(MERCHANT_ID)).thenReturn(Optional.of(merchantWithWebhook(null)));

        service.deliver(MERCHANT_ID, EVENT_TYPE, PAYLOAD);

        verifyNoInteractions(restTemplate);
        verify(auditPort, never()).logWebhookDelivery(anyString(), anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should skip silently when webhookUrl is blank")
    void shouldSkipWhenBlankWebhookUrl() {
        when(merchantQueryPort.findById(MERCHANT_ID)).thenReturn(Optional.of(merchantWithWebhook("   ")));

        service.deliver(MERCHANT_ID, EVENT_TYPE, PAYLOAD);

        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("Should skip silently when merchant is not found")
    void shouldSkipWhenMerchantNotFound() {
        when(merchantQueryPort.findById(MERCHANT_ID)).thenReturn(Optional.empty());

        service.deliver(MERCHANT_ID, EVENT_TYPE, PAYLOAD);

        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("Should skip silently when merchantId is missing")
    void shouldSkipWhenMerchantIdMissing() {
        service.deliver(null, EVENT_TYPE, PAYLOAD);
        service.deliver("  ", EVENT_TYPE, PAYLOAD);

        verifyNoInteractions(merchantQueryPort, restTemplate, auditPort);
    }

    @Test
    @DisplayName("Should swallow unexpected errors (delivery must never break consumption)")
    void shouldSwallowUnexpectedErrors() {
        when(merchantQueryPort.findById(MERCHANT_ID)).thenThrow(new IllegalStateException("database down"));

        assertThatCode(() -> service.deliver(MERCHANT_ID, EVENT_TYPE, PAYLOAD)).doesNotThrowAnyException();

        verifyNoInteractions(restTemplate);
    }
}
