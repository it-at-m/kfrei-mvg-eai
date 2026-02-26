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
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Interface for the service to communicate with the Application KFrei.
 *
 * @author felix.haala
 */
public interface KfreiRestApiServiceI {

    /**
     * Checks if an Antrag with the given information exists.
     *
     * @param antragId       ID of the Antrag
     * @param geburtsdatum   Geburtsdatum in the Antrag
     * @param originUserName Username from authentication, used for tracing
     * @param requestId      Generated ID, used for tracing
     * @return Mono of {@link KfreiResponseDto} for further processing
     */
    Mono<KfreiResponseDto> existsAntrag(long antragId, LocalDate geburtsdatum, String originUserName, String requestId);

}
