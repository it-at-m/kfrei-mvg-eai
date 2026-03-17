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
import de.muenchen.oss.kfreimvgeai.dto.MvgResponseDto;
import de.muenchen.oss.kfreimvgeai.mapper.DefaultMapper;
import de.muenchen.oss.kfreimvgeai.security.KfreiMvgEaiRoles;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Contains all endpoints for handling Anträge in the EAI.
 *
 * @author felix.haala
 */
@RestController
@RequestMapping("/api/v1/antraege")
@Tag(name = "Anträge", description = "Operations for Anträge")
@RequiredArgsConstructor
@Slf4j
public class AntragController {

    private final KfreiRestApiServiceI kfreiRestApiService;
    private final DefaultMapper mapper;

    @GetMapping(path = "/{antragId}/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Verify an Antrag",
            description = "Verifies that an Antrag with given information exists",
            security = @SecurityRequirement(name = OpenApiConfiguration.SCHEME_NAME)
    )
    @ApiResponses(
        {
                @ApiResponse(
                        responseCode = "200",
                        description = "Antrag exists",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = MvgResponseDto.class),
                                examples = @ExampleObject(
                                    """
                                            { "berechtigungAb": "2025-12-24", "befristungBis": "2026-12-01" }
                                            """
                                )
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Bad Request",
                        content = @Content(
                                mediaType = "text/plain",
                                schema = @Schema(type = "string"),
                                examples = @ExampleObject("Invalid argument [propertyName=%s]")
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "Antrag does not exist",
                        content = @Content
                ),
                @ApiResponse(
                        responseCode = "500",
                        description = "Processing error occurred",
                        content = @Content
                )
        }
    )
    @PreAuthorize("hasRole('" + KfreiMvgEaiRoles.ANTRAG_READ + "')")
    public ResponseEntity<MvgResponseDto> existsAntrag(
            @Parameter(description = "ID of Antrag", required = true) @PathVariable long antragId,
            @Parameter(description = "Geburtsdatum in Antrag [YYYY-MM-DD]", required = true) @RequestParam @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            ) LocalDate geburtsdatum,
            Authentication authentication) {
        String originUserName = authentication.getName();
        String requestId = UUID.randomUUID().toString();
        log.info("Validating Antrag [antragId={}, geburtsdatum={}, request=[originUserName={}, requestId={}]]",
                antragId, geburtsdatum, originUserName, requestId);

        try {
            KfreiResponseDto kfreiResponseDto = kfreiRestApiService.existsAntrag(antragId, geburtsdatum, originUserName, requestId);
            MvgResponseDto mvgResponseDto = this.mapper.kfreiResponseDtoToMvgResponseDto(kfreiResponseDto);

            log.info("Antrag found, returning info [antragId={}, geburtsdatum={}, request=[originUserName={}, requestId={}]]",
                    antragId, geburtsdatum, originUserName, requestId);
            return ResponseEntity.ok(mvgResponseDto);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("Antrag not found [antragId={}, geburtsdatum={}, request=[originUserName={}, requestId={}]]",
                        antragId, geburtsdatum, originUserName, requestId);
                return ResponseEntity.notFound().build();
            }

            log.error("Unknown error [antragId={}, geburtsdatum={}, request=[originUserName={}, requestId={}]]",
                    antragId, geburtsdatum, originUserName, requestId, ex);
            return ResponseEntity.internalServerError().build();
        }
    }

}
