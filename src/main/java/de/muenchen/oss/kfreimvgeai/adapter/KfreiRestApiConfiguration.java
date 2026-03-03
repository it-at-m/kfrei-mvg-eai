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

import de.muenchen.oss.kfreimvgeai.config.AppConfigurationProperties;
import de.muenchen.oss.kfreimvgeai.config.KfreiRestApiType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for setting up the KfreiRestApi.
 *
 * @author felix.haala
 */
@Configuration
public class KfreiRestApiConfiguration {

    /**
     * Creates an instance of KfreiRestApiService or its mock implementation  based on the configuration properties.
     *
     * @param appConfigurationProperties       the configuration properties for the kfrei-mvg-eai
     * @param clientRegistrationRepository     the repository for client registration information
     * @param oAuth2AuthorizedClientRepository the repository for authorized OAuth2 clients
     * @return a configured instance implementing the KfreiRestApiServiceI
     */
    @Bean
    KfreiRestApiServiceI kfreiRestApiService(AppConfigurationProperties appConfigurationProperties,
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService oAuth2AuthorizedClientRepository) {
        String baseUrl = appConfigurationProperties.getKfreiRestApi().getBaseUrl();
        KfreiRestApiType kfreiRestApiType = appConfigurationProperties.getKfreiRestApi().getType();

        return switch (kfreiRestApiType) {
            case REST -> {
                ReactiveOAuth2AuthorizedClientManager manager = kfreiRestApiAuthorizedClientManager(clientRegistrationRepository,
                        oAuth2AuthorizedClientRepository);
                WebClient webClient = kfreiRestApiwebClient(manager, baseUrl);
                yield new KfreiRestApiService(baseUrl, webClient);
            }
            case MOCK -> new KfreiRestApiServiceMock(baseUrl);
            case null -> throw new IllegalStateException("KfreiRestApiType is not defined");
        };
    }

    /**
     * Configures the ClientRegistrationRepository and OAuth2AuthorizedClientRepository for the KfreiRestApiService to enable OAuth2 authentication.
     *
     * @param clientRegistrationRepository     the repository for client registration information
     * @param oAuth2AuthorizedClientRepository the repository for authorized OAuth2 clients
     * @return the configured OAuth2AuthorizedClientManager
     */
    ReactiveOAuth2AuthorizedClientManager kfreiRestApiAuthorizedClientManager(ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService oAuth2AuthorizedClientRepository) {
        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository, oAuth2AuthorizedClientRepository);
        ReactiveOAuth2AuthorizedClientProvider provider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }

    /**
     * Configures a WebClient for the KfreiRestApiService to enable OAuth2 authentication.
     *
     * @param oAuth2AuthorizedClientManager the manager for authorized OAuth2 clients
     * @param baseUrl                       the base URL for the KfreiRestApiService
     * @return the configured WebClient instance
     */
    WebClient kfreiRestApiwebClient(ReactiveOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager, String baseUrl) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2 = new ServerOAuth2AuthorizedClientExchangeFilterFunction(oAuth2AuthorizedClientManager);
        oauth2.setDefaultClientRegistrationId("kfrei-rest-api");
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter(oauth2)
                .build();
    }

}
