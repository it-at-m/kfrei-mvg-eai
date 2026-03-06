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
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;

/**
 * Service for communicating with the KFrei Application.
 *
 * @author felix.haala
 */
@Slf4j
public class KfreiRestApiService implements KfreiRestApiServiceI {

    private final RestClient restClient;

    public KfreiRestApiService(RestClient restClient) {
        this.restClient = restClient;
        log.debug("Initialized KfreiRestApiService");
    }

    public KfreiResponseDto existsAntrag(long antragId, LocalDate geburtsdatum, String originUserName, String requestId) throws RestClientResponseException {
        String path = "/antraege/{antragId}/exists";

        log.debug("Requesting KfreiRestApi [path={}, antragId={}, geburtsdatum={}, originUserName={}, requestId={}]",
                path, antragId, geburtsdatum, originUserName, requestId);

        KfreiResponseDto kfreiResponseDto = this.restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("geburtsdatum", geburtsdatum)
                        .build(antragId))
                .headers(h -> {
                    h.add("x-origin-user-name", originUserName);
                    h.add("x-request-id", requestId);
                })
                .retrieve()
                .body(KfreiResponseDto.class);

        log.debug("Response [kfreiResponseDto={}, requestId={}]", kfreiResponseDto, requestId);
        return kfreiResponseDto;
    }

}
