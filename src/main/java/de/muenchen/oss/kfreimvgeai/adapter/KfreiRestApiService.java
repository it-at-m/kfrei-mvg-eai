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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Service for communication with the KFrei Application.
 * <p>
 * This service handles all interactions with the KFrei application through its KfreiRestApi.
 *
 * @author felix.haala
 */
@Slf4j
public class KfreiRestApiService implements KfreiRestApiServiceI {

    private final String baseUrl;
    private final WebClient webClient;

    public KfreiRestApiService(String baseUrl, WebClient webClient) {
        this.baseUrl = baseUrl;
        this.webClient = webClient;
        log.info("Created KfreiRestApiService [baseUrl={}]", this.baseUrl);
    }

    /**
     * Retrieves information from the API endpoint of KFrei.
     * <p>
     * This method checks the existence of an Antrag by making a request to the KfreiRestApi.
     *
     * @param antragId       the ID of the Antrag to check
     * @param geburtsdatum   the Geburtsdatum in the Antrag
     * @param originUserName the userName of the requester
     * @param requestId      the unique identifier for the request
     * @return a Mono containing the response from the KfreiRestApi as a KfreiResponseDto
     */
    public Mono<KfreiResponseDto> existsAntrag(long antragId, LocalDate geburtsdatum, String originUserName, String requestId) {
        String path = "/antraege/{antragId}/exists";

        log.debug("Requesting KfreiRestApi [baseUrl={}, path={}, antragId={}, geburtsdatum={}, originUserName={}, requestId={}]",
                this.baseUrl, path, antragId, geburtsdatum, originUserName, requestId);

        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("geburtsdatum", geburtsdatum)
                        .build(antragId))
                .headers(h -> {
                    h.add("x-origin-user-name", originUserName);
                    h.add("x-request-id", requestId);
                })
                .retrieve()
                .bodyToMono(KfreiResponseDto.class)
                .log("KfreiRestApiService.existsAntrag", java.util.logging.Level.WARNING)
                .doOnNext(kfreiResponseDto -> log.debug("Response [kfreiResponseDto={}, requestId={}]", kfreiResponseDto, requestId))
                .timeout(Duration.ofSeconds(10));
    }

}
