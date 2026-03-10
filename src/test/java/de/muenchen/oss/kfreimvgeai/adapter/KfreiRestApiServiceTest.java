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

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.muenchen.oss.kfreimvgeai.dto.KfreiResponseDto;
import de.muenchen.oss.kfreimvgeai.properties.AppConfigurationProperties;
import de.muenchen.oss.kfreimvgeai.properties.KfreiRestApiConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link WireMockTest} class for the KfreiRestApiService.
 *
 * @author felix.haala
 */
@SpringBootTest(
        classes = { KfreiRestApiConfiguration.class },
        properties = { "app.kfrei-rest-api.base-url=http://localhost:8090/api/v2" }
)
@WireMockTest(httpPort = 8090)
@ActiveProfiles({ "no-security" })
@EnableConfigurationProperties({ AppConfigurationProperties.class, KfreiRestApiConfigurationProperties.class })
class KfreiRestApiServiceTest {

    @Autowired
    KfreiRestApiServiceI sut;

    @Test
    void existsAntrag200OkTest() {
        long antragId = 1337L;
        LocalDate geburtsdatum = LocalDate.of(2019, 12, 30);
        String originUserName = "yetAnotherName";
        String requestId = UUID.randomUUID().toString();

        String requestPath = "/api/v2/antraege/1337/exists?geburtsdatum=2019-12-30";
        String responseJson = """
                    { "gueltigAb": "2025-12-24", "gueltigBis": "2026-12-01" }
                """;
        WireMock.stubFor(WireMock.get(requestPath)
                .withHeader("x-origin-user-name", WireMock.equalTo(originUserName))
                .withHeader("x-request-id", WireMock.equalTo(requestId))
                .willReturn(WireMock.okForContentType("application/json", responseJson)));

        KfreiResponseDto kfreiResponseDto = sut.existsAntrag(antragId, geburtsdatum, originUserName, requestId);

        assertNotNull(kfreiResponseDto);
        assertEquals(LocalDate.of(2025, 12, 24), kfreiResponseDto.gueltigAb());
        assertEquals(LocalDate.of(2026, 12, 1), kfreiResponseDto.gueltigBis());

        WireMock.verify(
                WireMock.getRequestedFor(WireMock.urlEqualTo(requestPath)));
    }

    @Test
    void existsAntrag404NotFoundTest() {
        long antragId = 1337L;
        LocalDate geburtsdatum = LocalDate.of(2019, 12, 30);
        String originUserName = "yetAnotherName";
        String requestId = UUID.randomUUID().toString();

        String requestPath = "/api/v2/antraege/1337/exists?geburtsdatum=2019-12-30";

        WireMock.stubFor(WireMock.get(requestPath)
                .withHeader("x-origin-user-name", WireMock.equalTo(originUserName))
                .withHeader("x-request-id", WireMock.equalTo(requestId))
                .willReturn(WireMock.notFound()));

        assertThrows(HttpClientErrorException.NotFound.class,
                () -> sut.existsAntrag(antragId, geburtsdatum, originUserName, requestId));

        WireMock.verify(
                WireMock.getRequestedFor(WireMock.urlEqualTo(requestPath)));
    }

    @ParameterizedTest
    @MethodSource("provideSomeErrors")
    void existsAntragMultipleErrorsTest(ResponseDefinitionBuilder wireMockResponseBuilder) {
        long antragId = 1337L;
        LocalDate geburtsdatum = LocalDate.of(2019, 12, 30);
        String originUserName = "yetAnotherName";
        String requestId = UUID.randomUUID().toString();

        String requestPath = "/api/v2/antraege/1337/exists?geburtsdatum=2019-12-30";

        WireMock.stubFor(WireMock.get(requestPath)
                .withHeader("x-origin-user-name", WireMock.equalTo(originUserName))
                .withHeader("x-request-id", WireMock.equalTo(requestId))
                .willReturn(wireMockResponseBuilder));

        assertThrows(HttpStatusCodeException.class,
                () -> sut.existsAntrag(antragId, geburtsdatum, originUserName, requestId));

        WireMock.verify(
                WireMock.getRequestedFor(WireMock.urlEqualTo(requestPath)));
    }

    static Stream<Arguments> provideSomeErrors() {
        return Stream.of(
                Arguments.of(WireMock.badRequest()),
                Arguments.of(WireMock.unauthorized()),
                Arguments.of(WireMock.forbidden()),
                Arguments.of(WireMock.notFound()),
                Arguments.of(WireMock.serverError()),
                Arguments.of(WireMock.serviceUnavailable()));
    }
}
