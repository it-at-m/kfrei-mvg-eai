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

import de.muenchen.oss.kfreimvgeai.adapter.KfreiRestApiServiceI;
import de.muenchen.oss.kfreimvgeai.dto.KfreiResponseDto;
import de.muenchen.oss.kfreimvgeai.mapper.DefaultMapperImpl;
import de.muenchen.oss.kfreimvgeai.properties.AppConfigurationProperties;
import de.muenchen.oss.kfreimvgeai.security.DefaultSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for the {@link AntragController} class.
 *
 * @author felix.haala
 */
@WebMvcTest(
        controllers = { AntragController.class },
        properties = { "app.resourceserver.client-id=" + AntragControllerTest.CLIENT_ID }
)
@Import({ DefaultSecurityConfiguration.class, DefaultMapperImpl.class, DefaultExceptionHandler.class })
@EnableConfigurationProperties({ AppConfigurationProperties.class })
class AntragControllerTest {

    public static final String CLIENT_ID = "kfrei-mvg-eai";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    KfreiRestApiServiceI mKfreiRestApiService;
    @MockitoBean
    JwtDecoder mJwtDecoder;
    @MockitoBean
    ClientRegistrationRepository mClientRegistrationRepository;

    @Test
    void existsAntrag200AntragExistsTest() throws Exception {
        KfreiResponseDto kfreiResponseDto = new KfreiResponseDto(
                LocalDate.of(2025, 12, 24),
                LocalDate.of(2026, 12, 1));

        when(mKfreiRestApiService
                .existsAntrag(eq(1337L), eq(LocalDate.of(2019, 12, 30)), eq("subject.mockito.user"), any()))
                        .thenReturn(kfreiResponseDto);

        setupMockedJwtAuthorizedWithRole();

        mockMvc.perform(get("/api/v1/antraege/{antragId}/exists", 1337)
                .queryParam("geburtsdatum", "2019-12-30")
                .header("Authorization", "Bearer dummy-token"))

                .andExpect(status().isOk())
                .andExpect(content().string(not(emptyString())))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.berechtigungAb").value("2025-12-24"))
                .andExpect(jsonPath("$.befristungBis").value("2026-12-01"));

        verify(mKfreiRestApiService, times(1))
                .existsAntrag(eq(1337L), eq(LocalDate.of(2019, 12, 30)), eq("subject.mockito.user"), any());

        verifyNoMoreInteractions(mKfreiRestApiService);
    }

    @Test
    void existsAntrag400BadRequestTest() throws Exception {
        setupMockedJwtAuthorizedWithRole();

        mockMvc.perform(get("/api/v1/antraege/{antragId}/exists", 1337)
                .queryParam("geburtsdatum", "2019-12-30d")
                .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(emptyString())))
                .andExpect(content().string("Invalid argument [propertyName=geburtsdatum, value=2019-12-30d]"));

        verifyNoInteractions(mKfreiRestApiService);
    }

    @Test
    void existsAntrag404AntragDoesNotExistTest() throws Exception {
        when(mKfreiRestApiService
                .existsAntrag(eq(1337L), eq(LocalDate.of(2019, 12, 30)), eq("subject.mockito.user"), any()))
                        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "don't care", HttpHeaders.EMPTY, null, null));

        setupMockedJwtAuthorizedWithRole();

        mockMvc.perform(get("/api/v1/antraege/{antragId}/exists", 1337)
                .queryParam("geburtsdatum", "2019-12-30")
                .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(emptyString()));

        verify(mKfreiRestApiService, times(1))
                .existsAntrag(eq(1337L), eq(LocalDate.of(2019, 12, 30)), eq("subject.mockito.user"), any());

        verifyNoMoreInteractions(mKfreiRestApiService);
    }

    @Test
    void existsAntrag401UnauthorizedTest() throws Exception {
        mockMvc.perform(get("/api/v1/antraege/{antragId}/exists", 1337)
                .queryParam("geburtsdatum", "2019-12-30"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(emptyString()));

        verifyNoInteractions(mKfreiRestApiService);
    }

    @Test
    void existsAntrag403ForbiddenTest() throws Exception {
        setupMockedJwtAuthorizedWithUnauthorizedRole();

        mockMvc.perform(get("/api/v1/antraege/{antragId}/exists", 1337)
                .queryParam("geburtsdatum", "2019-12-30")
                .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(emptyString()));

        verifyNoInteractions(mKfreiRestApiService);
    }

    @ParameterizedTest
    @MethodSource("provideSomeErrors")
    void existsAntragMultipleErrorsTest(HttpStatusCodeException httpStatusCodeException) throws Exception {
        setupMockedJwtAuthorizedWithRole();

        when(mKfreiRestApiService
                .existsAntrag(eq(1337L), eq(LocalDate.of(2019, 12, 30)), eq("subject.mockito.user"), any()))
                        .thenThrow(httpStatusCodeException);

        mockMvc.perform(get("/api/v1/antraege/{antragId}/exists", 1337)
                .queryParam("geburtsdatum", "2019-12-30")
                .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().is5xxServerError())
                .andExpect(content().string(emptyString()));

        verify(mKfreiRestApiService, times(1))
                .existsAntrag(eq(1337L), eq(LocalDate.of(2019, 12, 30)), eq("subject.mockito.user"), any());

        verifyNoMoreInteractions(mKfreiRestApiService);
    }

    static Stream<Arguments> provideSomeErrors() {
        return Stream.of(
                Arguments.of(new HttpClientErrorException(HttpStatus.BAD_GATEWAY, "don't care", HttpHeaders.EMPTY, null, null)),
                Arguments.of(new HttpClientErrorException(HttpStatus.METHOD_NOT_ALLOWED, "don't care", HttpHeaders.EMPTY, null, null)),
                Arguments.of(new HttpClientErrorException(HttpStatus.CONFLICT, "don't care", HttpHeaders.EMPTY, null, null)),
                Arguments.of(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "don't care", HttpHeaders.EMPTY, null, null)),
                Arguments.of(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE, "don't care", HttpHeaders.EMPTY, null, null)),
                Arguments.of(new HttpServerErrorException(HttpStatus.NOT_IMPLEMENTED, "don't care", HttpHeaders.EMPTY, null, null)));
    }

    void setupMockedJwtAuthorizedWithRole() {
        Jwt mockJwt = new Jwt(
                "dummy-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of(
                        "sub", "subject.mockito.user",
                        "preferred_username", "mockito.user",
                        "resource_access", Map.of(
                                CLIENT_ID, Map.of("roles", List.of("ANTRAG_READ")))));

        when(mJwtDecoder.decode(any())).thenReturn(mockJwt);
    }

    void setupMockedJwtAuthorizedWithUnauthorizedRole() {
        Jwt mockJwt = new Jwt(
                "dummy-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of(
                        "sub", "subject.mockito.user",
                        "preferred_username", "mockito.user",
                        "resource_access", Map.of(
                                CLIENT_ID, Map.of("roles", "DONT_CARE"))));

        when(mJwtDecoder.decode(any())).thenReturn(mockJwt);
    }

}
