/*
 * The MIT License
 * Copyright © 2026 Landeshauptstadt München | it@M
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.muenchen.oss.kfreimvgeai.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ExpressionJwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import reactor.core.publisher.Mono;

/**
 * Default security configuration for setting up filter chains in Spring.
 * <p>
 * This class configures security filters to manage authentication and authorization for incoming requests, ensuring that the application adheres to security
 * best practices.
 *
 * @author felix.haala
 */
@Configuration
@EnableWebFluxSecurity
class DefaultSecurityConfiguration {

    /**
     * Security filter chain for securing API calls.
     * <p>
     * This configuration sets up security measures specifically for API endpoints, ensuring that proper authentication and authorization mechanisms are
     * enforced for all incoming API requests.
     *
     * @param http the ServerHttpSecurity object used for configuring security
     * @return the configured SecurityWebFilterChain
     */
    @Bean
    @Order(1)
    @Profile("!no-security")
    SecurityWebFilterChain apiFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/api/**"))
                // Disable CSRF. API is stateless and uses token-based authentication (Authorization header)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // Allow role-based access to the API
                .authorizeExchange(auth -> auth.anyExchange().hasAnyRole(KfreiMvgEaiRoles.getAll()))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(this.jwtAuthenticationConverter()))
                );
        return http.build();
    }

    /**
     * Converter to extract OAuth2 roles from the JWT access token.
     *
     * @return the configured Converter
     */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        Expression expression = new SpelExpressionParser().parseExpression("['resource_access']['kfrei-mvg-eai']['roles']");

        ExpressionJwtGrantedAuthoritiesConverter authoritiesConverter = new ExpressionJwtGrantedAuthoritiesConverter(expression);
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return new ReactiveJwtAuthenticationConverterAdapter(jwtConverter);
    }

    /**
     * Security filter chain for securing web requests.
     * <p>
     * This configuration sets up security configurations for web-based interactions, applying authentication and authorization rules to protect web endpoints
     * and user sessions.
     *
     * @param http the ServerHttpSecurity object used for configuring security
     * @return the configured SecurityWebFilterChain
     */
    @Bean()
    @Order(2)
    @Profile("!no-security")
    SecurityWebFilterChain webFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/**"))
                // Disable CSRF. API is stateless and uses token-based authentication (Authorization header)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        // Allow authenticated usage of OpenAPI-Specification
                        .pathMatchers("/v3/api-docs*/**").authenticated()
                        .pathMatchers("/swagger-ui/**").authenticated()
                        .pathMatchers("/swagger-ui.html").authenticated()
                        // Allow public usage of liveness
                        .pathMatchers("/actuator/health/liveness").permitAll()
                        // Allow authenticated usage of health
                        .pathMatchers("/actuator/health/**").authenticated()
                        // Deny anything else
                        .anyExchange().denyAll())
                .oauth2Login(Customizer.withDefaults())
                .build();
    }

    /**
     * Security configuration that allows unrestricted access.
     * <p>
     * This configuration is used to disable security measures, allowing the application to run without authentication for development or testing purposes. It
     * should not be used in production environments.
     *
     * @param http the ServerHttpSecurity object used for configuring security
     * @return the configured SecurityWebFilterChain
     */
    @Bean
    @Profile("no-security")
    SecurityWebFilterChain noSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth ->
                        auth.anyExchange().permitAll()
                ).build();
    }

}