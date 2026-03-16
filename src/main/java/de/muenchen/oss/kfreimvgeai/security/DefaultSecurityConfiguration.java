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

import de.muenchen.oss.kfreimvgeai.properties.AppConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.ExpressionJwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Default security configuration for setting up filter chains in Spring.
 *
 * @author felix.haala
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class DefaultSecurityConfiguration {

    /**
     * Security filter chain for securing API calls.
     *
     * @param http the HttpSecurity object used for configuring security
     * @return the configured SecurityFilterChain
     */
    @Bean
    @Order(1)
    @Profile("!no-security-kfrei-mvg-eai")
    SecurityFilterChain apiFilterChain(HttpSecurity http, AppConfigurationProperties appConfiguration) {
        http
                .securityMatcher("/api/**")
                // Disable CSRF. API is stateless and uses token-based authentication (Authorization header)
                .csrf(AbstractHttpConfigurer::disable)
                // Allow role-based access to the API
                .authorizeHttpRequests(auth -> auth.anyRequest().hasAnyRole(KfreiMvgEaiRoles.getAll()))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(this.jwtAuthenticationConverter(appConfiguration))));
        return http.build();
    }

    /**
     * Converter to extract OAuth2 roles from the JWT access token.
     *
     * @return the configured Converter
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter(AppConfigurationProperties appConfiguration) {
        String clientId = appConfiguration.getResourceserver().getClientId();
        String format = "['resource_access']['%s']['roles']".formatted(clientId);

        Expression expression = new SpelExpressionParser().parseExpression(format);

        ExpressionJwtGrantedAuthoritiesConverter authoritiesConverter = new ExpressionJwtGrantedAuthoritiesConverter(expression);
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return jwtConverter;
    }

    /**
     * Security filter chain for securing web requests.
     *
     * @param http the HttpSecurity object used for configuring security
     * @return the configured SecurityFilterChain
     */
    @Bean()
    @Order(2)
    @Profile("!no-security-kfrei-mvg-eai")
    SecurityFilterChain webFilterChain(HttpSecurity http) {
        return http
                .securityMatcher("/**")
                // Disable CSRF. API is stateless and uses token-based authentication (Authorization header)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Allow public usage of OpenAPI-Specification
                        .requestMatchers("/v3/api-docs*/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        // Allow public usage of health
                        .requestMatchers("/actuator/health/**").permitAll()
                        // Allow public usage of info
                        .requestMatchers("/actuator/info").permitAll()
                        // Deny anything else
                        .anyRequest().denyAll())
                .build();
    }

    /**
     * Security configuration that allows unrestricted access.
     *
     * @param http the HttpSecurity object used for configuring security
     * @return the configured SecurityFilterChain
     */
    @Bean
    @Profile("no-security-kfrei-mvg-eai")
    SecurityFilterChain noSecurityFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
    }

}
