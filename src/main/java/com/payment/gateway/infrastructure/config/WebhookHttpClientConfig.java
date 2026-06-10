package com.payment.gateway.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client used for outbound merchant webhook delivery.
 * Short timeouts so slow/unreachable merchant endpoints never stall
 * Kafka consumer threads (or e2e tests).
 */
@Configuration
public class WebhookHttpClientConfig {

    @Bean("webhookRestTemplate")
    public RestTemplate webhookRestTemplate(
            @Value("${webhook.delivery.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${webhook.delivery.read-timeout-ms:2000}") int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }
}
