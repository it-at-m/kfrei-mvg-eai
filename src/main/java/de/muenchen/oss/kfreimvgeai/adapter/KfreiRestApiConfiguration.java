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
package de.muenchen.oss.kfreimvgeai.adapter;

import de.muenchen.oss.kfreimvgeai.properties.AppConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

/**
 * Configuration class for setting up the KfreiRestApi.
 *
 * @author felix.haala
 */
@Configuration
@Slf4j
public class KfreiRestApiConfiguration {

    /**
     * Creates an instance of KfreiRestApiService based on the specified configuration properties.
     *
     * @param restClient the RestClient instance used for making REST API calls to the KfreiRestApi
     * @return a configured instance of KfreiRestApiService
     */
    @Bean
    @Profile("!mock-kfrei-rest-api")
    KfreiRestApiService kfreiRestApiService(RestClient restClient) {
        log.info("Initializing KfreiRestApiService");
        return new KfreiRestApiService(restClient);
    }

    /**
     * Creates an instance of KfreiRestApiServiceMock based on the specified configuration properties.
     *
     * @return a configured instance of KfreiRestApiServiceMock ready for use in tests
     */
    @Bean
    @Profile("mock-kfrei-rest-api")
    KfreiRestApiServiceMock kfreiRestApiServiceMock() {
        log.info("Initializing KfreiRestApiServiceMock");
        return new KfreiRestApiServiceMock();
    }

    /**
     * Configures a RestClient for the KfreiRestApiService to enable OAuth2 authentication.
     *
     * @param clientRegistrationRepository the repository for managing client registrations for OAuth2
     * @param oAuth2AuthorizedClientService the service for managing authorized OAuth2 clients and their
     *            tokens
     * @param appConfigurationProperties the application configuration properties used to customize the
     *            setup of the RestClient
     * @return the configured RestClient instance
     */
    @Bean
    @Profile("!mock-kfrei-rest-api & !no-security-kfrei-rest-api")
    RestClient kfreiRestApiRestClient(ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
            AppConfigurationProperties appConfigurationProperties) {
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                oAuth2AuthorizedClientService);

        var provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        manager.setAuthorizedClientProvider(provider);

        var oauth2Interceptor = new OAuth2ClientHttpRequestInterceptor(manager);

        String baseUrl = appConfigurationProperties.getKfreiRestApi().getBaseUrl();
        log.info("Initializing RestClient with OAuth2 configuration [baseUrl={}]", baseUrl);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(oauth2Interceptor)
                .defaultRequest(request -> request.attributes(clientRegistrationId("kfrei-rest-api")))
                .build();
    }

    /**
     * Configures a RestClient for the KfreiRestApiService without security features.
     *
     * @param appConfigurationProperties the application configuration properties
     * @return a RestClient instance configured for the KfreiRestApi without security features, ready
     *         for use in non-secure environments
     */
    @Bean
    @Profile("!mock-kfrei-rest-api & no-security-kfrei-rest-api")
    RestClient kfreiRestApiRestClientNoSecurity(AppConfigurationProperties appConfigurationProperties) {
        String baseUrl = appConfigurationProperties.getKfreiRestApi().getBaseUrl();
        log.info("Initializing RestClient without security configuration [baseUrl={}]", baseUrl);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

}
