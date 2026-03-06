package com.payment.gateway.infrastructure.commons.rest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@DisplayName("RequestValidationFilter Tests")
@ExtendWith(MockitoExtension.class)
class RequestValidationFilterTest {

    private RequestValidationFilter filter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new RequestValidationFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Nested
    @DisplayName("GET Requests")
    class GetRequestTests {

        @Test
        @DisplayName("should pass through normal GET request")
        void shouldPassThroughGetRequest() throws ServletException, IOException {
            request.setMethod("GET");
            request.setRequestURI("/api/payments");

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(200);
        }

        @Test
        @DisplayName("should pass through GET request regardless of content type")
        void shouldPassThroughGetRegardlessOfContentType() throws ServletException, IOException {
            request.setMethod("GET");
            request.setContentType("text/plain");

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("POST Requests - Valid")
    class PostValidTests {

        @Test
        @DisplayName("should pass through POST with valid JSON content type")
        void shouldPassThroughPostWithJsonContentType() throws ServletException, IOException {
            request.setMethod("POST");
            request.setContentType("application/json");
            request.setContent("{\"key\":\"value\"}".getBytes());

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should pass through POST with JSON content type and charset")
        void shouldPassThroughPostWithJsonContentTypeAndCharset() throws ServletException, IOException {
            request.setMethod("POST");
            request.setContentType("application/json;charset=UTF-8");
            request.setContent("{\"key\":\"value\"}".getBytes());

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should pass through POST with no body")
        void shouldPassThroughPostWithNoBody() throws ServletException, IOException {
            request.setMethod("POST");

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("POST Requests - Content Too Large")
    class PostContentTooLargeTests {

        @Test
        @DisplayName("should reject POST with body exceeding 1MB with 413")
        void shouldRejectPostWithBodyExceeding1MB() throws ServletException, IOException {
            request.setMethod("POST");
            request.setContentType("application/json");
            // Set content length to just over 1MB
            byte[] largeContent = new byte[1_048_577];
            request.setContent(largeContent);

            filter.doFilter(request, response, filterChain);

            assertThat(response.getStatus()).isEqualTo(413);
            assertThat(response.getContentType()).isEqualTo("application/json");
            assertThat(response.getContentAsString()).contains("Request body too large");
            verifyNoInteractions(filterChain);
        }

        @Test
        @DisplayName("should pass through POST with body exactly 1MB")
        void shouldPassThroughPostWithBodyExactly1MB() throws ServletException, IOException {
            request.setMethod("POST");
            request.setContentType("application/json");
            byte[] exactContent = new byte[1_048_576];
            request.setContent(exactContent);

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("POST Requests - Wrong Content Type")
    class PostWrongContentTypeTests {

        @Test
        @DisplayName("should reject POST with wrong content type with 415")
        void shouldRejectPostWithWrongContentType() throws ServletException, IOException {
            request.setMethod("POST");
            request.setContentType("text/plain");
            request.setContent("some text content".getBytes());

            filter.doFilter(request, response, filterChain);

            assertThat(response.getStatus()).isEqualTo(415);
            assertThat(response.getContentType()).isEqualTo("application/json");
            assertThat(response.getContentAsString()).contains("Content-Type must be application/json");
            verifyNoInteractions(filterChain);
        }

        @Test
        @DisplayName("should reject POST with XML content type with 415")
        void shouldRejectPostWithXmlContentType() throws ServletException, IOException {
            request.setMethod("POST");
            request.setContentType("application/xml");
            request.setContent("<xml>data</xml>".getBytes());

            filter.doFilter(request, response, filterChain);

            assertThat(response.getStatus()).isEqualTo(415);
            verifyNoInteractions(filterChain);
        }
    }

    @Nested
    @DisplayName("PUT and PATCH Requests")
    class PutAndPatchTests {

        @Test
        @DisplayName("should reject PUT with body exceeding 1MB")
        void shouldRejectPutWithLargeBody() throws ServletException, IOException {
            request.setMethod("PUT");
            request.setContentType("application/json");
            byte[] largeContent = new byte[1_048_577];
            request.setContent(largeContent);

            filter.doFilter(request, response, filterChain);

            assertThat(response.getStatus()).isEqualTo(413);
            verifyNoInteractions(filterChain);
        }

        @Test
        @DisplayName("should reject PATCH with wrong content type")
        void shouldRejectPatchWithWrongContentType() throws ServletException, IOException {
            request.setMethod("PATCH");
            request.setContentType("text/html");
            request.setContent("<html>data</html>".getBytes());

            filter.doFilter(request, response, filterChain);

            assertThat(response.getStatus()).isEqualTo(415);
            verifyNoInteractions(filterChain);
        }

        @Test
        @DisplayName("should pass through PUT with valid JSON")
        void shouldPassThroughPutWithValidJson() throws ServletException, IOException {
            request.setMethod("PUT");
            request.setContentType("application/json");
            request.setContent("{\"update\":true}".getBytes());

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("DELETE Requests")
    class DeleteRequestTests {

        @Test
        @DisplayName("should pass through DELETE request without validation")
        void shouldPassThroughDeleteRequest() throws ServletException, IOException {
            request.setMethod("DELETE");
            request.setRequestURI("/api/payments/123");

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }
    }
}
