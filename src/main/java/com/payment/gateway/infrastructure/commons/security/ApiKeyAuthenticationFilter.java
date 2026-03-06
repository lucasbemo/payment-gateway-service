package com.payment.gateway.infrastructure.commons.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filter that authenticates requests using an API key header.
 * Not auto-registered as a bean. Will be wired into SecurityConfig in Phase 5.
 */
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-Api-Key";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // API key validation will be implemented in Phase 5
            // For now, this filter passes through without blocking
            log.debug("API key present in request: {}****", apiKey.substring(0, Math.min(4, apiKey.length())));
        }

        filterChain.doFilter(request, response);
    }
}
