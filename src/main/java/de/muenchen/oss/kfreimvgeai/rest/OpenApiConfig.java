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
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

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
                description = "REST API for communicating with kfrei-mvg-eai secured via OAuth2 Client Credentials."
        ),
        security = @SecurityRequirement(name = OpenApiConfig.OAUTH2_SCHEME_NAME)

)
@SecurityScheme(
        name = OpenApiConfig.OAUTH2_SCHEME_NAME,
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(
                clientCredentials = @OAuthFlow(
                        tokenUrl = "https://example.com/auth/realms/example/protocol/openid-connect/token",
                        scopes = {
                                @OAuthScope(
                                        name = "roles",
                                        description = "Requires scope [roles]"
                                )
                                ,
                                @OAuthScope(
                                        name = "ANTRAG_READ",
                                        description = "Requires role [ANTRAG_READ] in [resource_access.client-name.roles]"
                                )
                        }
                )
        )
)
public class OpenApiConfig {

    public final static String OAUTH2_SCHEME_NAME = "oAuth2ClientCredentials";

}