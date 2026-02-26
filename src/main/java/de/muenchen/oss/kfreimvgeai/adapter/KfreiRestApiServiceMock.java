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

import de.muenchen.oss.kfreimvgeai.dto.KfreiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Mock implementation of the service for communication with the Application KFrei.
 *
 * @author felix.haala
 */
@Slf4j
public class KfreiRestApiServiceMock implements KfreiRestApiServiceI {

    private final String baseUrl;

    public KfreiRestApiServiceMock(String baseUrl) {
        this.baseUrl = baseUrl;
        log.debug("Created KfreiRestApiServiceMock [baseUrl={}]", this.baseUrl);
    }

    public Mono<KfreiResponseDto> existsAntrag(long antragId, LocalDate geburtsdatum, String originUserName, String requestId) {
        String path = "/antraege/{antragId}/exists";

        log.info("Requesting KfreiRestApiMock [baseUrl={}, path={}, antragId={}, geburtsdatum={}, originUserName={}, requestId={}]",
                this.baseUrl, path, antragId, geburtsdatum, originUserName, requestId);

        if (antragId == 0L) {
            return Mono.error(WebClientResponseException.create(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", null, null, null));
        }

        KfreiResponseDto kfreiResponseDto = mockRequest(antragId, geburtsdatum);

        if (kfreiResponseDto == null) {
            return Mono.error(WebClientResponseException.create(
                    HttpStatus.NOT_FOUND.value(), "Not Found", null, null, null));
        }

        log.debug("Response [kfreiResponseDto={}, requestId={}]", kfreiResponseDto, requestId);
        return Mono.just(kfreiResponseDto);
    }

    private static KfreiResponseDto mockRequest(long antragId, LocalDate geburtsdatum) {
        // Schaltjahr
        if (antragId == 1111111111L && geburtsdatum.isEqual(LocalDate.of(1989, 11, 30))) {
            return new KfreiResponseDto(LocalDate.of(2024, 2, 29), LocalDate.of(2024, 12, 29));
        }
        // Vorjahr
        if (antragId == 2222222222L && geburtsdatum.isEqual(LocalDate.of(1989, 2, 28))) {
            new KfreiResponseDto(LocalDate.of(2025, 2, 28), LocalDate.of(2025, 12, 28));
        }
        // Aktuelles Jahr
        if (antragId == 3333333333L && geburtsdatum.isEqual(LocalDate.of(1989, 9, 30))) {
            return new KfreiResponseDto(LocalDate.of(2026, 1, 30), LocalDate.of(2026, 12, 31));
        }
        // Folgejahr
        if (antragId == 4444444444L && geburtsdatum.isEqual(LocalDate.of(1989, 10, 31))) {
            return new KfreiResponseDto(LocalDate.of(2027, 1, 30), LocalDate.of(2027, 12, 31));
        }
        
        return null;
    }

}
