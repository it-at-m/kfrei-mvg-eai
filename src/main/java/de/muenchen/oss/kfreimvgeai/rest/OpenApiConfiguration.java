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

import de.muenchen.oss.kfreimvgeai.properties.AppConfigurationProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.apache.commons.lang3.StringUtils;
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
public class OpenApiConfiguration {

    public static final String SCHEME_NAME = "bearer";
    public static final String DEFAULT_TOKEN_URL = "https://sso.example.com/auth/realms/example/protocol/openid-connect/token";

    @Bean
    @Profile("!no-security-kfrei-mvg-eai")
    public OpenAPI customOpenAPI(AppConfigurationProperties appConfiguration) {
        String tokenUrl = StringUtils.defaultIfBlank(appConfiguration.getSwaggerUi().getTokenUrl(), DEFAULT_TOKEN_URL);

        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme(SCHEME_NAME)
                .bearerFormat("JWT")
                .description(
                        """
                                This API requires an access token issued by the SSO system.  The token must be sent in the HTTP Authorization header:

                                    Authorization: Bearer <access_token>

                                The token will include all client roles assigned to your application. Specific roles (e.g. antragread) can be found in the token claim:

                                    resource_access.<client-name>.roles

                                Obtain a token using client credentials

                                    curl -X POST %s -H "Content-Type: application/x-www-form-urlencoded" -d "grant_type=client_credentials" -d "client_id=<client-id>" -d "client_secret=<client-secret>" --data-urlencode "scope=openid roles"

                                Click the "Authorize" button in Swagger UI and paste the token.
                                """
                                .formatted(tokenUrl));

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(SCHEME_NAME, bearerScheme))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SCHEME_NAME));
    }

}
