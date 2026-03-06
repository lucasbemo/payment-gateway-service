package com.payment.gateway.infrastructure.commons.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CorrelationIdInterceptor Tests")
class CorrelationIdInterceptorTest {

    private CorrelationIdInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private final Object handler = new Object();

    @BeforeEach
    void setUp() {
        interceptor = new CorrelationIdInterceptor();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Nested
    @DisplayName("preHandle - Correlation ID Generation")
    class PreHandleGenerationTests {

        @Test
        @DisplayName("should generate correlation ID when none present in request")
        void shouldGenerateCorrelationIdWhenNonePresent() {
            boolean result = interceptor.preHandle(request, response, handler);

            assertThat(result).isTrue();
            String correlationId = MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY);
            assertThat(correlationId).isNotNull();
            assertThat(correlationId).isNotBlank();
        }

        @Test
        @DisplayName("should generate UUID format correlation ID")
        void shouldGenerateUuidFormatCorrelationId() {
            interceptor.preHandle(request, response, handler);

            String correlationId = MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY);
            assertThat(correlationId).matches(
                    "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("should set correlation ID in response header")
        void shouldSetCorrelationIdInResponseHeader() {
            interceptor.preHandle(request, response, handler);

            String responseHeader = response.getHeader(CorrelationIdInterceptor.CORRELATION_ID_HEADER);
            assertThat(responseHeader).isNotNull();
            assertThat(responseHeader).isNotBlank();
        }

        @Test
        @DisplayName("should generate unique correlation IDs for different requests")
        void shouldGenerateUniqueCorrelationIds() {
            interceptor.preHandle(request, response, handler);
            String firstId = MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY);

            MDC.clear();
            MockHttpServletRequest secondRequest = new MockHttpServletRequest();
            MockHttpServletResponse secondResponse = new MockHttpServletResponse();
            interceptor.preHandle(secondRequest, secondResponse, handler);
            String secondId = MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY);

            assertThat(firstId).isNotEqualTo(secondId);
        }
    }

    @Nested
    @DisplayName("preHandle - Correlation ID Reuse")
    class PreHandleReuseTests {

        @Test
        @DisplayName("should reuse correlation ID from request header")
        void shouldReuseCorrelationIdFromHeader() {
            String existingId = "existing-correlation-id-123";
            request.addHeader(CorrelationIdInterceptor.CORRELATION_ID_HEADER, existingId);

            interceptor.preHandle(request, response, handler);

            String correlationId = MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY);
            assertThat(correlationId).isEqualTo(existingId);
        }

        @Test
        @DisplayName("should set reused correlation ID in response header")
        void shouldSetReusedCorrelationIdInResponseHeader() {
            String existingId = "trace-abc-def-456";
            request.addHeader(CorrelationIdInterceptor.CORRELATION_ID_HEADER, existingId);

            interceptor.preHandle(request, response, handler);

            assertThat(response.getHeader(CorrelationIdInterceptor.CORRELATION_ID_HEADER))
                    .isEqualTo(existingId);
        }

        @Test
        @DisplayName("should generate new ID when header is blank")
        void shouldGenerateNewIdWhenHeaderIsBlank() {
            request.addHeader(CorrelationIdInterceptor.CORRELATION_ID_HEADER, "   ");

            interceptor.preHandle(request, response, handler);

            String correlationId = MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY);
            assertThat(correlationId).isNotBlank();
            assertThat(correlationId.trim()).isNotEqualTo("");
        }
    }

    @Nested
    @DisplayName("afterCompletion - MDC Cleanup")
    class AfterCompletionTests {

        @Test
        @DisplayName("should clear MDC correlation ID after completion")
        void shouldClearMdcAfterCompletion() {
            interceptor.preHandle(request, response, handler);
            assertThat(MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY)).isNotNull();

            interceptor.afterCompletion(request, response, handler, null);

            assertThat(MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY)).isNull();
        }

        @Test
        @DisplayName("should clear MDC even when exception occurred")
        void shouldClearMdcEvenOnException() {
            interceptor.preHandle(request, response, handler);

            interceptor.afterCompletion(request, response, handler, new RuntimeException("test error"));

            assertThat(MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY)).isNull();
        }

        @Test
        @DisplayName("should not fail when MDC is already empty")
        void shouldNotFailWhenMdcAlreadyEmpty() {
            MDC.clear();

            interceptor.afterCompletion(request, response, handler, null);

            assertThat(MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY)).isNull();
        }
    }

    @Nested
    @DisplayName("Constants")
    class ConstantsTests {

        @Test
        @DisplayName("should use correct header name")
        void shouldUseCorrectHeaderName() {
            assertThat(CorrelationIdInterceptor.CORRELATION_ID_HEADER).isEqualTo("X-Correlation-Id");
        }

        @Test
        @DisplayName("should use correct MDC key")
        void shouldUseCorrectMdcKey() {
            assertThat(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY).isEqualTo("correlationId");
        }
    }
}
