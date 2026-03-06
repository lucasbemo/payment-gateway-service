package com.payment.gateway.infrastructure.commons.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filter that authenticates requests using an API key header.
 * Validates the API key against the merchant database and sets up security context.
 */
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-Api-Key";
    private static final String API_SECRET_HEADER = "X-Api-Secret";

    private final ApiKeyAuthService apiKeyAuthService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);
        String apiSecret = request.getHeader(API_SECRET_HEADER);

        if (apiKey != null && apiSecret != null) {
            try {
                ApiKeyValidationResult result = apiKeyAuthService.validateApiKey(apiKey, apiSecret);

                if (result.isValid()) {
                    List<SimpleGrantedAuthority> authorities = result.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .toList();

                    var authentication = new UsernamePasswordAuthenticationToken(
                            result.getMerchantId(),
                            null,
                            authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Authenticated merchant: {} with roles: {}", result.getMerchantId(), result.getRoles());
                } else {
                    log.warn("Invalid API key: {}", result.getMessage());
                }
            } catch (Exception e) {
                log.error("Error validating API key: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
