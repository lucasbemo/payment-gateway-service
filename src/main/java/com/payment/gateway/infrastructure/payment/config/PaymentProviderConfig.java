package com.payment.gateway.infrastructure.payment.config;

import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort;
import com.payment.gateway.application.refund.port.out.ExternalRefundProviderPort;
import com.payment.gateway.application.webhook.port.in.WebhookProcessingPort;
import com.payment.gateway.infrastructure.payment.adapter.out.provider.stripe.StripePaymentProvider;
import com.payment.gateway.infrastructure.refund.adapter.out.provider.stripe.StripeRefundProviderAdapter;
import com.payment.gateway.infrastructure.webhook.adapter.in.handler.StripeWebhookHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PaymentProviderConfig {

    @Bean
    @ConditionalOnProperty(name = "payment.provider", havingValue = "stripe")
    @Primary
    public ExternalPaymentProviderPort stripePaymentProvider(
            @Value("${stripe.api-key}") String apiKey,
            @Value("${stripe.api-base-url:https://api.stripe.com}") String apiBaseUrl) {
        return new StripePaymentProvider(apiKey, apiBaseUrl);
    }

    @Bean
    @ConditionalOnProperty(name = "payment.provider", havingValue = "stripe")
    @Primary
    public ExternalRefundProviderPort stripeRefundProvider(
            @Value("${stripe.api-key}") String apiKey) {
        return new StripeRefundProviderAdapter(apiKey);
    }

    @Bean
    @ConditionalOnProperty(name = "payment.provider", havingValue = "stripe")
    @Primary
    public WebhookProcessingPort stripeWebhookHandler(
            @Value("${stripe.webhook-secret:}") String webhookSecret) {
        return new StripeWebhookHandler(webhookSecret);
    }

    @Bean
    @ConditionalOnMissingBean(ExternalRefundProviderPort.class)
    public ExternalRefundProviderPort stubRefundProvider() {
        return new com.payment.gateway.infrastructure.refund.adapter.out.provider.StubRefundProvider();
    }

    @Bean
    @ConditionalOnMissingBean(WebhookProcessingPort.class)
    public WebhookProcessingPort stubWebhookHandler() {
        return new com.payment.gateway.infrastructure.webhook.adapter.in.handler.StubWebhookHandler();
    }
}