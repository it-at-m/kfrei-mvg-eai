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
package de.muenchen.oss.kfreimvgeai.rest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class for the OpenAPI documentation of the kfrei-mvg-eai API.
 *
 * @author felix.haala
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "kfrei-mvg-eai API",
                version = "v1.0.0",
                description = "REST API for communicating with kfrei-mvg-eai."
        )
)
public class OpenApiConfig {

    public static final String OAUTH2_SCHEME_NAME = "oAuth2ClientCredentials";

    @Bean
    @Profile("!no-security")
    public OpenAPI customOpenAPI(
            @Value("${app.swagger-ui.token-url:https://sso.example.com/auth/realms/example/protocol/openid-connect/token}") String tokenUrl) {
        SecurityScheme oauthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("""
                        This API requires an access token issued by the SSO system.  The token must be sent in the HTTP Authorization header:
                        
                            Authorization: Bearer <access_token>
                        
                        Example: obtain a token using client credentials
                        
                            curl -X POST %s -H "Content-Type: application/x-www-form-urlencoded" -d "grant_type=client_credentials" -d "client-id:<client-id>" -d "client_secret=<client-secret>" -d "scope=roles"
                        
                        The token will include all client roles assigned to your application. Specific roles, such as [ANTRAG_READ], can be found in the token claim:
                        
                            resource_access.<client-name>.roles
                        """.formatted(tokenUrl))
                .flows(new OAuthFlows()
                        .clientCredentials(new OAuthFlow()
                                .tokenUrl(tokenUrl)
                                .scopes(new Scopes()
                                        .addString("roles", "All client roles, including [ANTRAG_READ], etc."))
                        )
                );

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(OAUTH2_SCHEME_NAME, oauthScheme))
                .addSecurityItem(new SecurityRequirement()
                        .addList(OAUTH2_SCHEME_NAME));
    }

}