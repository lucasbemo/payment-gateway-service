package com.payment.gateway.infrastructure.config;

import com.payment.gateway.infrastructure.commons.security.ApiKeyAuthService;
import com.payment.gateway.infrastructure.commons.security.ApiKeyAuthenticationFilter;
import com.payment.gateway.infrastructure.commons.security.JwtAuthenticationFilter;
import com.payment.gateway.infrastructure.commons.security.JwtTokenProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("!e2e")
public class SecurityConfig {

    /**
     * Security filter chain for local/development with open access.
     */
    @Bean
    @Profile({"local", "dev"})
    public SecurityFilterChain localSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/actuator/**")
                        .permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**")
                        .permitAll()
                        .anyRequest()
                        .permitAll());
        return http.build();
    }

    /**
     * Security filter chain for default profile (restricted access).
     */
    @Bean
    @Profile("!e2e & !local & !dev & !prod")
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/actuator/**")
                        .permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**")
                        .permitAll()
                        .requestMatchers("/api/v1/auth/**")
                        .permitAll()
                        .requestMatchers("/api/v1/health/**")
                        .permitAll()
                        .requestMatchers("/api/v1/**")
                        .authenticated()
                        .anyRequest()
                        .authenticated());
        return http.build();
    }

    /**
     * Security filter chain for production with API key and JWT authentication.
     * Activated by the {@code prod} profile — the same profile used by
     * application-prod.yml and docker-compose.prod.yml. Unauthenticated requests to
     * protected endpoints receive 401 (not 403) so API clients can react correctly.
     */
    @Configuration
    @Profile("prod")
    @RequiredArgsConstructor
    static class ProductionSecurityConfig {

        private final ApiKeyAuthService apiKeyAuthService;
        private final JwtTokenProvider jwtTokenProvider;

        @Bean
        public SecurityFilterChain productionSecurityFilterChain(
                HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .cors(cors -> cors.configurationSource(corsConfigurationSource))
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.requestMatchers("/actuator/**")
                            .permitAll()
                            .requestMatchers(
                                    "/swagger-ui/**",
                                    "/swagger-ui.html",
                                    "/v3/api-docs/**",
                                    "/swagger-resources/**",
                                    "/webjars/**")
                            .permitAll()
                            .requestMatchers("/api/v1/auth/**")
                            .permitAll()
                            .requestMatchers("/api/v1/health/**")
                            .permitAll()
                            .anyRequest()
                            .authenticated())
                    .exceptionHandling(
                            ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                    .addFilterBefore(
                            new ApiKeyAuthenticationFilter(apiKeyAuthService),
                            UsernamePasswordAuthenticationFilter.class)
                    .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), ApiKeyAuthenticationFilter.class);
            return http.build();
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow all origins for development - restrict in production
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("X-Idempotency-Key", "X-Correlation-Id"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
