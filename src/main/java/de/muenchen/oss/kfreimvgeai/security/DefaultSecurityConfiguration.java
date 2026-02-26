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
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Default security configuration for setting up filter chains in Spring.
 * <p>
 * This class configures security filters to manage authentication and authorization for incoming requests, ensuring that the application adheres to security
 * best practices.
 *
 * @author felix.haala
 */
@Configuration
@EnableWebSecurity
class DefaultSecurityConfiguration {

    /**
     * Security filter chain for securing API calls.
     * <p>
     * This class configures security measures specifically for API endpoints, ensuring that proper authentication and authorization mechanisms are enforced for
     * all incoming API requests.
     *
     * @param http the HttpSecurity object used for configuring security
     * @return the configured SecurityFilterChain
     */
    @Bean
    @Order(1)
    @Profile("!no-security")
    SecurityFilterChain apiChain(HttpSecurity http) {
        http
                .securityMatcher("/api/**")
                // Disable CSRF. API is stateless and uses token-based authentication (Authorization header)
                .csrf(AbstractHttpConfigurer::disable)
                // Allow authenticated usage of api
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));
        return http.build();
    }

    /**
     * Security filter chain for securing web requests.
     * <p>
     * This class sets up security configurations for web-based interactions, applying authentication and authorization rules to protect web endpoints and user
     * sessions.
     *
     * @param http the HttpSecurity object used for configuring security
     * @return the configured SecurityFilterChain
     */
    @Bean()
    @Order(2)
    @Profile("!no-security")
    SecurityFilterChain webChain(HttpSecurity http) {
        return http
                .securityMatcher("/**")
                // Disable CSRF. API is stateless and uses token-based authentication (Authorization header)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Allow authenticated usage of OpenAPI-Specification
                        .requestMatchers("/v3/api-docs*/**").authenticated()
                        .requestMatchers("/swagger-ui/**").authenticated()
                        .requestMatchers("/swagger-ui.html").authenticated()
                        // Allow public usage of health
                        .requestMatchers("/actuator/health/**").permitAll()
                        // Deny anything else
                        .anyRequest().denyAll())
                .oauth2Login(Customizer.withDefaults())
                .build();
    }

    /**
     * Security configuration that allows unrestricted access.
     * <p>
     * This class is used to disable security measures, allowing the application to run without authentication for development or testing purposes. This
     * configuration should not be used in production environments.
     *
     * @param http the HttpSecurity object used for configuring security
     * @return the configured SecurityFilterChain
     */
    @Bean()
    @Profile("no-security")
    SecurityFilterChain noSecurityFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().anonymous()
                ).build();
    }

}