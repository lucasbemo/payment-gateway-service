package com.payment.gateway.infrastructure.commons.rest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that validates incoming requests for common issues
 * such as content-type validation and request size limits.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestValidationFilter extends OncePerRequestFilter {

    private static final int MAX_CONTENT_LENGTH = 1_048_576; // 1MB

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        // Validate content length for POST/PUT/PATCH
        if (isWriteMethod(request.getMethod())) {
            int contentLength = request.getContentLength();
            if (contentLength > MAX_CONTENT_LENGTH) {
                log.warn("Request rejected: content length {} exceeds maximum {}", contentLength, MAX_CONTENT_LENGTH);
                response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Request body too large\",\"data\":null}");
                return;
            }

            if (contentLength > 0 && !isJsonContentType(request.getContentType())) {
                log.warn("Request rejected: unsupported content type '{}'", request.getContentType());
                response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Content-Type must be application/json\",\"data\":null}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWriteMethod(String method) {
        return "POST".equalsIgnoreCase(method) ||
               "PUT".equalsIgnoreCase(method) ||
               "PATCH".equalsIgnoreCase(method);
    }

    private boolean isJsonContentType(String contentType) {
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }
}
